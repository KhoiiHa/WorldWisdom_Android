package com.example.projektworldwisdom.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private var allQuotes: List<Quote> = emptyList()
    private var currentCategoryKey: String? = null
    private var currentSearchQuery: String = ""

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
            },
            // ✅ Fallback: falls du noch eine ältere Adapter-Signatur offen hast
            onFavoriteClick = { quote: Quote ->
                viewModel.toggleFavorite(quote)
            }
        )

        binding.quotesList.layoutManager = LinearLayoutManager(requireContext())
        binding.quotesList.adapter = quotesAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ladeanzeige
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Zitate-Liste beobachten
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            allQuotes = quotes ?: emptyList()
            applyFilters()
            renderDailyAffirmation(allQuotes)
        }

        // Suchfeld
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nicht benötigt
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString().orEmpty()
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {
                // nicht benötigt
            }
        })

        // Kategorie-Filter (Material Chips)
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID

            currentCategoryKey = when (checkedId) {
                R.id.filter_Society -> "society"
                R.id.filter_success -> "success"
                R.id.filter_work -> "work"
                R.id.filter_wisdom -> "wisdom"
                R.id.filter_gratitude -> "gratitude"
                R.id.filter_alle, View.NO_ID -> null
                else -> null
            }

            applyFilters()
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
            binding.affirmationText.text = getString(R.string.loading_daily_quote)
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

    private fun applyFilters() {
        val search = currentSearchQuery.trim()
        val key = currentCategoryKey

        val mappedCategories: List<String> = when (key) {

            // Society: Gesellschaft, Werte, Politik, Gerechtigkeit
            "society" -> listOf(
                "Weltanschauung",
                "Politik",
                "Gerechtigkeit",
                "Freiheit",
                "Gleichheit",
                "Wahrheit",
                "Charakter",
                "Zusammenarbeit"
            )

            // Success: Erfolg, Motivation, Herausforderungen, Dranbleiben
            "success" -> listOf(
                "Erfolg",
                "Motivation",
                "Meisterschaft",
                "Herausforderungen",
                "Zweifel",
                "Entscheidungen"
            )

            // Work: Arbeit & Umsetzung
            "work" -> listOf(
                "Arbeit",
                "Produktivität",
                "Innovation",
                "Problemlösung"
            )

            // Wisdom: Wissen, Philosophie, Lernen, Wissenschaft
            "wisdom" -> listOf(
                "Wissen",
                "Weisheit",
                "Philosophie",
                "Bildung",
                "Wissenschaft",
                "Intelligenz",
                "Fragen",
                "Fehler",
                "Zeit",
                "Menschlichkeit",
                "Selbstkenntnis",
                "Selbstentdeckung"
            )

            // Gratitude: Leben, Liebe, Hoffnung, Frieden, Veränderung
            "gratitude" -> listOf(
                "Leben",
                "Leben und Prioritäten",
                "Liebe",
                "Liebe und Mitgefühl",
                "Hoffnung",
                "Frieden",
                "Gewaltlosigkeit",
                "Veränderung",
                "Inspiration",
                "Träume",
                "Zukunft",
                "Kreativität",
                "Verführung"
            )

            else -> emptyList() // "Alle"
        }

        val filtered = allQuotes
            .asSequence()
            .filter { quote ->
                // Kategorie-Filter
                if (mappedCategories.isEmpty()) {
                    true
                } else {
                    mappedCategories.any { mapped ->
                        mapped.equals(quote.category, ignoreCase = true)
                    }
                }
            }
            .filter { quote ->
                // Search-Filter
                if (search.isEmpty()) {
                    true
                } else {
                    quote.quote.contains(search, ignoreCase = true) ||
                        quote.author.contains(search, ignoreCase = true)
                }
            }
            .toList()

        quotesAdapter.updateQuotes(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}