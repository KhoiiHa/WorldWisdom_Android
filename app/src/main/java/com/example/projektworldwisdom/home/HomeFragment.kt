package com.example.projektworldwisdom.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // RecyclerView + Adapter
        quotesAdapter = QuoteAdapter(
            onQuoteClick = { quote ->
                navigateToAuthorDetails(quote)
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
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Basis-Quotes (für Daily Affirmation)
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            renderDailyAffirmation(quotes.orEmpty())
        }

        // Gefilterte Quotes (Phase 2) — Home Feed
        viewModel.filteredQuotes.observe(viewLifecycleOwner) { quotes ->
            quotesAdapter.updateQuotes(quotes.orEmpty())
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
        }

        // Kategorie-Filter (Material Chips) → ViewModel (Phase 2)
        // Defensive: sorgt für konsistentes Verhalten, auch wenn XML mal nicht gesetzt ist.
        binding.chipGroupFilters.isSingleSelection = true

        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isProgrammaticChipUpdate) return@setOnCheckedStateChangeListener
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID

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
        }

        // Fehler anzeigen & danach zurücksetzen
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
        binding.affirmationAuthor.text = "– ${quote.author}"
        binding.affirmationHint.visibility = View.VISIBLE

        // Tap auf die Card → Author Details
        binding.dailyAffirmationCard.setOnClickListener {
            navigateToAuthorDetails(quote)
        }
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        // Minimal, aber "echt": wir geben vorhandene Daten direkt mit.
        // Später können wir hier optional auf echtes Detail-Fetching umschalten.
        val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(
            authorSlug = quote.author,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}