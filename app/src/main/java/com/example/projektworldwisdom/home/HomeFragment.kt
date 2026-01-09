package com.example.projektworldwisdom.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var quotesAdapter: QuoteAdapter

    // Guards to prevent feedback loops when we update UI from LiveData observers
    private var isProgrammaticSearchUpdate = false
    private var isProgrammaticChipUpdate = false

    // Simple UI state to drive Loading / Content / Empty
    private var latestIsLoading: Boolean = false
    private var latestFilteredCount: Int = 0
    private var latestQuery: String = ""
    private var latestCategoryFilter: SharedViewModel.CategoryFilter = SharedViewModel.CategoryFilter.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // RecyclerView + Adapter
        quotesAdapter = QuoteAdapter(
            onQuoteClick = { quote ->
                navigateToQuoteDetails(quote)
            },
            // ✅ Preferred: Adapter liefert den Zielzustand (true=speichern, false=entfernen)
            // Wir nutzen hier bewusst weiterhin toggleFavorite, weil der Zielzustand bereits
            // aus dem aktuellen Quote-State abgeleitet wurde.
            onFavoriteToggle = { quote: Quote, _ ->
                viewModel.toggleFavorite(quote)
            }
        )

        binding.quotesList.layoutManager = LinearLayoutManager(requireContext())
        binding.quotesList.adapter = quotesAdapter
        binding.quotesList.setHasFixedSize(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ladeanzeige
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            latestIsLoading = isLoading
            renderHomeState()
        }

        // Basis-Quotes (für Daily Affirmation)
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            renderDailyAffirmation(quotes.orEmpty())
        }

        // Gefilterte Quotes (Phase 2) — Home Feed
        viewModel.filteredQuotes.observe(viewLifecycleOwner) { quotes ->
            val safe = quotes.orEmpty()
            latestFilteredCount = safe.size
            quotesAdapter.updateQuotes(safe)
            renderHomeState()
        }

        // Suchfeld → ViewModel (Phase 2)
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            if (isProgrammaticSearchUpdate) return@doOnTextChanged
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }

        // IME Search: Keyboard schließen + Fokus entfernen
        binding.searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(v)
                v.clearFocus()
                // Kleiner UX-Boost: nach „Suchen“ wieder oben anfangen
                binding.quotesList.scrollToPosition(0)
                true
            } else {
                false
            }
        }

        // Restore UI state when returning (z.B. nach Navigation)
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            val current = binding.searchBar.text?.toString().orEmpty()
            latestQuery = query
            if (current != query) {
                isProgrammaticSearchUpdate = true
                binding.searchBar.setText(query)
                // Keep cursor at end for a nicer UX
                binding.searchBar.setSelection(binding.searchBar.text?.length ?: 0)
                isProgrammaticSearchUpdate = false

                // Wenn wir per Reset auf "leer" gehen: Keyboard zu + Liste nach oben
                if (query.isBlank()) {
                    hideKeyboard(binding.searchBar)
                    binding.searchBar.clearFocus()
                    binding.quotesList.scrollToPosition(0)
                }
            }
            renderHomeState()
        }

        // Kategorie-Filter (Material Chips) → ViewModel (Phase 2)
        // Defensive: sorgt für konsistentes Verhalten, auch wenn XML mal nicht gesetzt ist.
        binding.chipGroupFilters.isSingleSelection = true

        binding.chipGroupFilters.isSelectionRequired = true

        // Ensure a sane default on first launch
        if (binding.chipGroupFilters.checkedChipId == View.NO_ID) {
            isProgrammaticChipUpdate = true
            binding.chipGroupFilters.check(R.id.filter_alle)
            isProgrammaticChipUpdate = false
            viewModel.setCategoryFilter(SharedViewModel.CategoryFilter.ALL)
        }

        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isProgrammaticChipUpdate) return@setOnCheckedStateChangeListener
            val checkedId = checkedIds.firstOrNull() ?: R.id.filter_alle

            val filter = when (checkedId) {
                R.id.filter_society -> SharedViewModel.CategoryFilter.SOCIETY
                R.id.filter_success -> SharedViewModel.CategoryFilter.SUCCESS
                R.id.filter_work -> SharedViewModel.CategoryFilter.WORK
                R.id.filter_wisdom -> SharedViewModel.CategoryFilter.WISDOM
                R.id.filter_gratitude -> SharedViewModel.CategoryFilter.GRATITUDE
                R.id.filter_alle, View.NO_ID -> SharedViewModel.CategoryFilter.ALL
                else -> SharedViewModel.CategoryFilter.ALL
            }

            viewModel.setCategoryFilter(filter)
            binding.quotesList.scrollToPosition(0)
        }

        // Restore UI state when returning
        viewModel.selectedCategoryFilter.observe(viewLifecycleOwner) { filter ->
            val targetChipId = when (filter) {
                SharedViewModel.CategoryFilter.SOCIETY -> R.id.filter_society
                SharedViewModel.CategoryFilter.SUCCESS -> R.id.filter_success
                SharedViewModel.CategoryFilter.WORK -> R.id.filter_work
                SharedViewModel.CategoryFilter.WISDOM -> R.id.filter_wisdom
                SharedViewModel.CategoryFilter.GRATITUDE -> R.id.filter_gratitude
                else -> R.id.filter_alle
            }

            // Avoid redundant check updates + prevent feedback loop
            if (binding.chipGroupFilters.checkedChipId != targetChipId) {
                isProgrammaticChipUpdate = true
                binding.chipGroupFilters.check(targetChipId)
                isProgrammaticChipUpdate = false
            }
            latestCategoryFilter = filter
            renderHomeState()
        }

        // Fehler anzeigen & danach zurücksetzen
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Empty-State CTA: reset search + filter
        binding.btnEmptyReset.setOnClickListener {
            binding.btnEmptyReset.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            resetHomeInputs()
        }
        // Initialize latestQuery at startup
        latestQuery = binding.searchBar.text?.toString().orEmpty()

        // Initial state render
        renderHomeState()
    }

    private fun renderHomeState() {
        val showLoading = latestIsLoading
        val showEmpty = !latestIsLoading && latestFilteredCount == 0

        binding.progressBar.visibility = if (showLoading) View.VISIBLE else View.GONE
        binding.emptyStateContainer.visibility = if (showEmpty) View.VISIBLE else View.GONE
        binding.quotesList.visibility = if (showEmpty || showLoading) View.GONE else View.VISIBLE

        if (showEmpty) {
            renderEmptyStateText()
        }
    }

    private fun renderEmptyStateText() {
        val query = latestQuery.trim()

        val filterLabelRes = when (latestCategoryFilter) {
            SharedViewModel.CategoryFilter.SOCIETY -> R.string.home_filter_society
            SharedViewModel.CategoryFilter.SUCCESS -> R.string.home_filter_success
            SharedViewModel.CategoryFilter.WORK -> R.string.home_filter_work
            SharedViewModel.CategoryFilter.WISDOM -> R.string.home_filter_wisdom
            SharedViewModel.CategoryFilter.GRATITUDE -> R.string.home_filter_gratitude
            SharedViewModel.CategoryFilter.ALL -> R.string.home_filter_all
        }
        val filterLabel = getString(filterLabelRes)

        // Title
        binding.emptyStateTitle.text = if (query.isNotBlank()) {
            getString(R.string.home_empty_title_no_results)
        } else {
            getString(R.string.home_empty_title_no_quotes)
        }

        // Subtitle
        binding.emptyStateSubtitle.text = when {
            query.isNotBlank() && latestCategoryFilter != SharedViewModel.CategoryFilter.ALL ->
                getString(R.string.home_empty_subtitle_query_and_filter, query, filterLabel)

            query.isNotBlank() ->
                getString(R.string.home_empty_subtitle_query, query)

            latestCategoryFilter != SharedViewModel.CategoryFilter.ALL ->
                getString(R.string.home_empty_subtitle_filter, filterLabel)

            else ->
                getString(R.string.home_empty_subtitle_default)
        }
    }

    private fun renderDailyAffirmation(quotes: List<Quote>) {
        // Minimal & deterministisch: „Quote of the Day“ aus der vorhandenen Liste
        // Kein Over-Engineering, keine extra API.
        if (quotes.isEmpty()) {
            binding.affirmationText.text = getString(R.string.daily_affirmation_loading)
            binding.affirmationAuthor.text = ""
            binding.affirmationHint.visibility = View.GONE
            binding.dailyAffirmationCard.setOnClickListener(null)
            return
        }

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % quotes.size
        val quote = quotes[index]

        // Card Content
        binding.affirmationText.text = quote.quote
        binding.affirmationAuthor.text = getString(R.string.daily_affirmation_author_format, quote.author)
        binding.affirmationHint.visibility = View.VISIBLE

        // Tap auf die Card → Quote Details (von dort optional weiter zu Author Details)
        binding.dailyAffirmationCard.setOnClickListener {
            binding.dailyAffirmationCard.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            navigateToQuoteDetails(quote)
        }
    }

    private fun navigateToQuoteDetails(quote: Quote) {
        val action = HomeFragmentDirections.actionHomeFragmentToQuoteDetailsFragment(
            quoteId = quote.id,
            author = quote.author.takeIf { it.isNotBlank() },
            quoteText = quote.quote.takeIf { it.isNotBlank() },
            category = quote.category.takeIf { it.isNotBlank() },
            tags = quote.tags.takeIf { it.isNotEmpty() }?.toTypedArray(),
            sourceUrl = quote.source.takeIf { it.isNotBlank() },
            isFavorite = quote.isFavorite
        )

        findNavController().navigate(action)
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        // Minimal, aber "echt": wir geben vorhandene Daten direkt mit.
        // Später können wir hier optional auf echtes Detail-Fetching umschalten.
        val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(
            authorSlug = quote.authorSlug,
            authorName = quote.author,
            // Kurzzeile im Header (nicht over-engineeren): Kategorie ist dafür okay.
            authorDescription = quote.category.takeIf { it.isNotBlank() },
            // Langer Text im Bio-Block
            authorBio = quote.description.takeIf { it.isNotBlank() },
            // Wikipedia / Quelle
            authorSourceUrl = quote.source.takeIf { it.isNotBlank() }
        )

        findNavController().navigate(action)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun resetHomeInputs() {
        // Clear search (triggers existing TextWatcher / ViewModel update)
        binding.searchBar.setText("")
        binding.searchBar.clearFocus()

        // Reset filter to “Alle”
        isProgrammaticChipUpdate = true
        binding.chipGroupFilters.check(R.id.filter_alle)
        isProgrammaticChipUpdate = false
        viewModel.setCategoryFilter(SharedViewModel.CategoryFilter.ALL)

        // Keep local state in sync
        latestQuery = ""
        latestCategoryFilter = SharedViewModel.CategoryFilter.ALL
        renderHomeState()

        // Clean reset feel
        binding.quotesList.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}