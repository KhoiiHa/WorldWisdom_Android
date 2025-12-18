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

        setupRecyclerView()
        setupExploreCategoriesUi()
        setupActions()
        setupObservers()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onQuoteClick = { quote: Quote ->
                navigateToAuthorDetails(quote)
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

        // Start with a stable baseline even before favorites arrive
        renderCategoryChips(emptyList())
    }

    private fun renderCategoryChips(quotes: List<Quote>) {
        val categories = quotes
            .map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

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
                val stillExists = categories.contains(want)
                if (!stillExists) {
                    selectedCategory = null
                    chipAll.isChecked = true
                } else {
                    // Find and check the matching chip
                    for (i in 0 until binding.chipGroupCategories.childCount) {
                        val v = binding.chipGroupCategories.getChildAt(i)
                        if (v is Chip && v.tag == want) {
                            v.isChecked = true
                            break
                        }
                    }
                }
            }
        }
    }

    private fun createCategoryChip(label: String, categoryValue: String?): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            isCheckedIconVisible = false
            // Tag is used to identify the category later
            tag = categoryValue

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategory = categoryValue
                    renderFromState()
                }
            }
        }
    }

    private fun applyCategoryFilter(quotes: List<Quote>): List<Quote> {
        val cat = selectedCategory
        if (cat.isNullOrBlank()) return quotes
        return quotes.filter { it.category.equals(cat, ignoreCase = true) }
    }

    private fun setupActions() {
        // Empty CTA → Reset Explore (Home filters + search) and go back to Home
        binding.btnEmptyCta.setOnClickListener {
            sharedViewModel.resetExplore()
            navigateToHome()
        }

        // Retry → clear error + reload
        binding.btnRetry.setOnClickListener { retryRender() }
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

        // Show category explorer only when there is something to explore
        binding.cardExploreCategories.isVisible = latestFavorites.isNotEmpty()

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

    private fun navigateToAuthorDetails(quote: Quote) {
        // Minimal & consistent with Home: we pass what we already have.
        // Later, we can switch to a real AuthorDetails fetch if needed.
        val args = bundleOf(
            "authorSlug" to quote.author,
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
        // Show a human-friendly count (e.g., “3 Favoriten”) only when > 0
        binding.tvCollectionCount.isVisible = count > 0
        binding.tvCollectionCount.text = if (count > 0) {
            resources.getQuantityString(R.plurals.collection_favorites_count, count, count)
        } else {
            ""
        }
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
}