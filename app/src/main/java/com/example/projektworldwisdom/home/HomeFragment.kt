package com.example.projektworldwisdom.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                val action = HomeFragmentDirections
                    .actionHomeFragmentToAuthorDetailsFragment(quote.author)
                findNavController().navigate(action)
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

        // Restore UI state when returning (z.B. nach Navigation)
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            val current = binding.searchBar.text?.toString().orEmpty()
            if (current != query) {
                isProgrammaticSearchUpdate = true
                binding.searchBar.setText(query)
                // Keep cursor at end for a nicer UX
                binding.searchBar.setSelection(binding.searchBar.text?.length ?: 0)
                isProgrammaticSearchUpdate = false
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

    private fun renderDailyAffirmation(quotes: List<Quote>) {
        // Minimal & deterministisch: „Quote of the Day“ aus der vorhandenen Liste
        // Kein Over-Engineering, keine extra API.
        if (quotes.isEmpty()) {
            binding.affirmationText.text = getString(R.string.daily_affirmation_loading)
            binding.dailyAffirmationCard.setOnClickListener(null)
            return
        }

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % quotes.size
        val quote = quotes[index]

        // Ein TextView reicht: Quote + Autor, damit wir kein zusätzliches Layout brauchen.
        binding.affirmationText.text = getString(
            R.string.daily_affirmation_text,
            quote.quote,
            quote.author
        )

        // Tap auf die Card → Author Details
        binding.dailyAffirmationCard.setOnClickListener {
            val action = HomeFragmentDirections
                .actionHomeFragmentToAuthorDetailsFragment(quote.author)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}