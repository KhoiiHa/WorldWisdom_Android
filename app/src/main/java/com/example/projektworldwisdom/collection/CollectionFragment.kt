package com.example.projektworldwisdom.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentCollectionBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel
import com.google.android.material.chip.Chip

class CollectionFragment : Fragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    // Shared app state (favorites + loading + error)
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var quoteAdapter: QuoteAdapter

    // Local UI state snapshot (keeps this Fragment lean, no extra ViewModel needed)
    private var latestFavorites: List<Quote> = emptyList()
    private var latestError: String? = null
    private var latestLoading: Boolean = false

    // Explore by Category (local filter for Favorites list)
    private var selectedCategory: String? = null
    private var isRebuildingChips: Boolean = false

    private fun readInitialCategoryFromArgs() {
        // Supports deep-links / future Category-Overview screen.
        // We accept multiple keys to stay flexible without breaking navigation.
        val raw = arguments?.getString("category")
            ?: arguments?.getString("selectedCategory")
            ?: arguments?.getString("initialCategory")

        selectedCategory = raw?.trim()?.takeIf { it.isNotEmpty() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readInitialCategoryFromArgs()

        listenForCategoryResult()

        setupRecyclerView()
        setupExploreCategoriesUi()
        setupActions()
        setupObservers()
    }

    private fun listenForCategoryResult() {
        // CategoryOverview → returns the selected category via SavedStateHandle.
        // We listen here so Collection updates immediately when user comes back.
        val navController = findNavController()
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return

        handle.getLiveData<String?>(KEY_SELECTED_CATEGORY).observe(viewLifecycleOwner) { value ->
            // Consume once to avoid re-triggering after rotations / re-attaching observers
            handle.remove<String?>(KEY_SELECTED_CATEGORY)

            selectedCategory = value?.trim()?.takeIf { it.isNotEmpty() }

            // Re-apply selection + filter immediately
            renderCategoryChips(latestFavorites)
            renderFromState()
        }
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onQuoteClick = { quote: Quote ->
                navigateToQuoteDetails(quote)
            },
            onFavoriteClick = { quote: Quote ->
                // In Favorites screen: ⭐ toggles add/remove
                sharedViewModel.toggleFavorite(quote)
            }
        )

        binding.recyclerViewCollection.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupExploreCategoriesUi() {
        // Keep it simple: single selection, and we provide an explicit “Alle” chip to reset.
        binding.chipGroupCategories.isSingleSelection = true
        binding.chipGroupCategories.isSelectionRequired = true

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (isRebuildingChips) return@setOnCheckedStateChangeListener

            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener

            selectedCategory = chip.tag as? String
            renderFromState()
        }

        // Start with a stable baseline even before favorites arrive
        renderCategoryChips(emptyList())
    }

    private fun renderCategoryChips(quotes: List<Quote>) {
        isRebuildingChips = true

        val categories = quotes
            .map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

        binding.chipGroupCategories.removeAllViews()

        // “Alle” = reset filter
        val chipAll = createCategoryChip(
            label = getString(R.string.collection_chip_all),
            categoryValue = null
        )
        binding.chipGroupCategories.addView(chipAll)

        // Real categories
        categories.forEach { category ->
            binding.chipGroupCategories.addView(
                createCategoryChip(label = category, categoryValue = category)
            )
        }

        // Keep selection consistent after chip rebuild
        val want = selectedCategory
        when {
            want == null -> chipAll.isChecked = true
            else -> {
                // If selected category no longer exists (e.g., favorites changed), fallback to “Alle”
                val stillExists = categories.any { it.equals(want, ignoreCase = true) }
                if (!stillExists) {
                    selectedCategory = null
                    chipAll.isChecked = true
                } else {
                    // Find and check the matching chip
                    for (i in 0 until binding.chipGroupCategories.childCount) {
                        val v = binding.chipGroupCategories.getChildAt(i)
                        if (v is Chip && (v.tag as? String)?.equals(want, ignoreCase = true) == true) {
                            v.isChecked = true
                            break
                        }
                    }
                }
            }
        }

        isRebuildingChips = false
    }

    private fun createCategoryChip(label: String, categoryValue: String?): Chip {
        return Chip(requireContext()).apply {
            id = View.generateViewId()
            text = label
            isCheckable = true
            isCheckedIconVisible = false
            // Tag is used to identify the category later
            tag = categoryValue
        }
    }

    private fun applyCategoryFilter(quotes: List<Quote>): List<Quote> {
        val cat = selectedCategory
        if (cat.isNullOrBlank()) return quotes
        return quotes.filter { it.category.trim().equals(cat, ignoreCase = true) }
    }

    private fun setupActions() {
        // Empty CTA → Reset Explore (Home filters + search) and go back to Home
        binding.btnEmptyCta.setOnClickListener {
            sharedViewModel.resetExplore()
            navigateToHome()
        }

        // Retry → clear error + reload
        binding.btnRetry.setOnClickListener { retryRender() }

        // Explore by Category → opens the CategoryOverview screen
        // Whole card is clickable (nice on touch devices)
        binding.cardExploreCategories.setOnClickListener {
            navigateToCategoryOverview()
        }

        // Explore by Category → explicit entry point (top-right “Übersicht” button)
        // This avoids relying on tapping the card background.
        binding.btnCategoryOverview.setOnClickListener {
            navigateToCategoryOverview()
        }
    }

    private fun setupObservers() {
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            latestLoading = isLoading == true
            renderFromState()
        }

        sharedViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            latestError = errorMsg
            renderFromState()
        }

        sharedViewModel.favoriteQuotes.observe(viewLifecycleOwner) { favorites ->
            latestFavorites = favorites.orEmpty()

            // Rebuild chips from the *current* favorites (keeps UI consistent)
            renderCategoryChips(latestFavorites)

            // If we have favorites, we prefer showing them over a stale error
            if (latestFavorites.isNotEmpty()) latestError = null

            renderFromState()
        }
    }

    private fun renderFromState() {
        val filtered = applyCategoryFilter(latestFavorites)
        updateHeaderCount(filtered.size)

        // Entry point should always be discoverable; the overview can show its own empty-state.
        binding.cardExploreCategories.isVisible = true
        binding.btnCategoryOverview.isVisible = true

        when {
            // Prefer showing content if we have it (even if a background reload is happening)
            filtered.isNotEmpty() -> renderContent(filtered)
            latestLoading -> renderLoading()
            latestError != null -> renderError(latestError)
            else -> renderEmpty()
        }
    }

    private fun retryRender() {
        latestError = null
        sharedViewModel.clearError()
        sharedViewModel.loadQuotes()
        renderFromState()
    }

    private fun navigateToQuoteDetails(quote: Quote) {
        // QuoteDetails requires SafeArgs; pass what we already have from the list item.
        val action = CollectionFragmentDirections
            .actionCollectionFragmentToQuoteDetailsFragment(
                quoteId = quote.id,
                quoteText = quote.quote,
                author = quote.author,
                category = quote.category,
                sourceUrl = quote.source.takeIf { it.isNotBlank() },
                tags = quote.tags.toTypedArray(),
                isFavorite = quote.isFavorite
            )
        findNavController().navigate(action)
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        // Minimal & consistent with Home: we pass what we already have.
        // Later, we can switch to a real AuthorDetails fetch if needed.
        val args = bundleOf(
            "authorSlug" to quote.authorSlug,
            "authorName" to quote.author,
            // Short header line
            "authorDescription" to quote.category.takeIf { it.isNotBlank() },
            // Long bio/description
            "authorBio" to quote.description.takeIf { it.isNotBlank() },
            // Source link
            "authorSourceUrl" to quote.source.takeIf { it.isNotBlank() }
        )

        findNavController().navigate(R.id.authorDetailsFragment, args)
    }

    private fun navigateToHome() {
        val navController = findNavController()

        // Prefer popping back to Home if it exists in the back stack.
        val popped = navController.popBackStack(R.id.homeFragment, false)
        if (!popped) {
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .build()
            navController.navigate(R.id.homeFragment, null, options)
        }
    }

    private fun navigateToCategoryOverview() {
        // Navigation to CategoryOverview is always allowed.

        val args = bundleOf(
            // Useful for future analytics / UI decisions in the overview
            "origin" to "collection",
            // Preselect current category (if any) when opening the overview
            "initialCategory" to selectedCategory
        )

        val navController = findNavController()

        // Prefer the explicit action if it exists; fallback to direct destination navigation.
        runCatching {
            navController.navigate(
                R.id.action_collectionFragment_to_categoryOverviewFragment,
                args
            )
        }.recoverCatching {
            navController.navigate(R.id.categoryOverviewFragment, args)
        }
    }

    private fun setUiState(
        showContent: Boolean,
        showEmpty: Boolean,
        showLoading: Boolean,
        showError: Boolean
    ) {
        binding.recyclerViewCollection.isVisible = showContent
        binding.layoutEmptyState.isVisible = showEmpty
        binding.progressLoading.isVisible = showLoading
        binding.layoutErrorState.isVisible = showError
    }

    private fun updateHeaderCount(count: Int) {
        // Always show the count for clarity (even 0), e.g. “0 Favoriten”.
        binding.tvCollectionCount.isVisible = true
        binding.tvCollectionCount.text = resources.getQuantityString(
            R.plurals.collection_favorites_count,
            count,
            count
        )
    }

    // --- UI State Rendering ---

    private fun renderLoading() {
        setUiState(
            showContent = false,
            showEmpty = false,
            showLoading = true,
            showError = false
        )

        // Optional: clear list while loading
        quoteAdapter.updateQuotes(emptyList())
    }

    private fun renderEmpty() {
        setUiState(
            showContent = false,
            showEmpty = true,
            showLoading = false,
            showError = false
        )

        quoteAdapter.updateQuotes(emptyList())
    }

    private fun renderError(message: String?) {
        setUiState(
            showContent = false,
            showEmpty = false,
            showLoading = false,
            showError = true
        )

        val msg = message?.takeIf { it.isNotBlank() }
        if (msg != null) {
            binding.tvErrorSubtitle.text = msg
        } else {
            binding.tvErrorSubtitle.setText(R.string.common_error_subtitle)
        }

        quoteAdapter.updateQuotes(emptyList())
    }

    private fun renderContent(quotes: List<Quote>) {
        setUiState(
            showContent = true,
            showEmpty = false,
            showLoading = false,
            showError = false
        )

        quoteAdapter.updateQuotes(quotes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        // Must match the key used by CategoryOverviewFragment when returning a result.
        const val KEY_SELECTED_CATEGORY = "selectedCategory"
    }
}