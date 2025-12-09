package com.example.projektworldwisdom.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentCollectionBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class CollectionFragment : Fragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    // Gemeinsames ViewModel mit Home-Fragment
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var quoteAdapter: QuoteAdapter

    // Volle Liste aus dem ViewModel (für Filter)
    private var allQuotes: List<Quote> = emptyList()

    // Merkt sich, welcher Kategorie-Tab gerade aktiv ist (society, success, work, wisdom, gratitude, all/null)
    private var currentCategoryKey: String? = null

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
        observeQuotes()
        setupCategoryChips()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter()
        binding.recyclerViewCollection.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }
    }

    private fun observeQuotes() {
        sharedViewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            allQuotes = quotes ?: emptyList()
            // Immer mit der aktuell gewählten Kategorie filtern
            applyFilters()
        }
    }

    private fun setupCategoryChips() {
        // Standard: "All" ist aktiv → wir zeigen alles
        binding.chipCategoryAll.isChecked = true
        currentCategoryKey = null

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()

            currentCategoryKey = when (checkedId) {
                R.id.chipCategorySociety -> "society"
                R.id.chipCategorySuccess -> "success"
                R.id.chipCategoryWork -> "work"
                R.id.chipCategoryWisdom -> "wisdom"
                R.id.chipCategoryGratitude -> "gratitude"
                R.id.chipCategoryAll, null -> null
                else -> null
            }

            applyFilters()
        }
    }

    /**
     * Wendet die aktuell gewählte Kategorie auf die volle Liste an.
     * Wenn keine Kategorie gewählt ist (All), werden alle Zitate angezeigt.
     * Wenn eine Kategorie keine Treffer hat, fallen wir auf die komplette Liste zurück.
     */
    private fun applyFilters() {
        val categoryKey = currentCategoryKey

        val filtered: List<Quote> = if (categoryKey == null) {
            allQuotes
        } else {
            filterByCategoryKey(allQuotes, categoryKey)
                .ifEmpty { allQuotes } // Fallback: lieber alles zeigen als einen komplett leeren Screen
        }

        quoteAdapter.updateQuotes(filtered)
    }

    /**
     * Mappt deine englischen Kategorien (Society, Success, Work, Wisdom, Gratitude)
     * auf die deutschen Kategorien aus deinem JSON (Weltanschauung, Motivation, Arbeit, Wissen, ...).
     *
     * Das ist eine pragmatische Zuordnung:
     * - lieber leicht überlappende Gruppen als "perfekt logisch, aber leer".
     */
    private fun filterByCategoryKey(quotes: List<Quote>, key: String): List<Quote> {
        val categoriesForKey: List<String> = when (key.lowercase()) {
            // Gesellschaft, Politik, Gerechtigkeit, Freiheit
            "society" -> listOf(
                "Weltanschauung",
                "Politik",
                "Gerechtigkeit",
                "Freiheit",
                "Gleichheit"
            )

            // Erfolg, Motivation, Meisterschaft
            "success" -> listOf(
                "Erfolg",
                "Motivation",
                "Meisterschaft"
            )

            // Arbeit & Produktivität
            "work" -> listOf(
                "Arbeit",
                "Produktivität"
            )

            // Wissen, Weisheit, Philosophie, Bildung
            "wisdom" -> listOf(
                "Wissen",
                "Weisheit",
                "Philosophie",
                "Bildung",
                "Intelligenz"
            )

            // Dankbarkeit / positive Lebensperspektive
            "gratitude" -> listOf(
                "Leben",
                "Liebe",
                "Hoffnung",
                "Inspiration",
                "Träume"
            )

            else -> emptyList()
        }

        // Wenn für den Key keine Kategorien hinterlegt sind,
        // geben wir die Liste unverändert zurück.
        if (categoriesForKey.isEmpty()) return quotes

        return quotes.filter { quote ->
            categoriesForKey.any { mappedCategory ->
                mappedCategory.equals(quote.category, ignoreCase = true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}