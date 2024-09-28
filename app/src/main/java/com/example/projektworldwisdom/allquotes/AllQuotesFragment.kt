package com.example.projektworldwisdom.allquotes


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentAllQuotesBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.google.android.material.snackbar.Snackbar

class AllQuotesFragment : Fragment() {

    // ViewModel für das Fragment bereitstellen
    private val viewModel: AllQuotesViewModel by viewModels {
        val database = QuoteDatabase.getDatabase(requireContext())
        val quoteDao = database.quoteDao()
        val repository = QuoteRepository(quoteDao)
        AllQuotesViewModelFactory(repository)
    }

    private lateinit var binding: FragmentAllQuotesBinding
    private lateinit var quoteAdapter: QuoteAdapter
    private lateinit var suggestionAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflating des Layouts für das Fragment
        binding = FragmentAllQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView einrichten
        setupRecyclerView()

        // LiveData beobachten und Adapter aktualisieren
        observeLiveData()

        // Filter-Keywords beobachten
        observeAvailableKeywords()

        // Suchleiste verknüpfen und Vorschläge einrichten
        initializeSearchBar()

        // Ladeanzeige und Fehlerbehandlung
        setupLoadingAndErrorHandling()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(emptyList(), emptyList(), viewModel::saveQuote)
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                quote.authorName?.let { authorName ->
                    findNavController().navigate(
                        AllQuotesFragmentDirections.actionAllQuotesFragmentToAuthorDetailsFragment(
                            authorName,
                            quote
                        )
                    )
                } ?: run {
                    Toast.makeText(context, "Autorname nicht verfügbar", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Hier die saveQuote-Funktion als Callback hinzufügen
        quoteAdapter.setOnSaveClickListener { quote ->
            saveQuote(quote)
        }

        binding.allQuotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }
    }

    private fun observeLiveData() {
        viewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            filteredQuotes?.let {
                quoteAdapter.updateData(it, viewModel.authors.value ?: emptyList())
            } ?: run {
                Snackbar.make(
                    requireView(),
                    "Fehler beim Laden der gefilterten Zitate",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeAvailableKeywords() {
        viewModel.availableKeywords.observe(viewLifecycleOwner) { keywords ->
            binding.filterContainer.removeAllViews() // Vorherige Filter-Views entfernen

            keywords?.let { keywordList ->
                if (keywordList.isEmpty()) {
                    Snackbar.make(requireView(), "Keine Schlüsselwörter verfügbar", Snackbar.LENGTH_SHORT).show()
                } else {
                    keywordList.forEach { keyword ->
                        val textView = TextView(requireContext()).apply {
                            text = keyword
                            setPadding(16, 8, 16, 8)

                            // OnClickListener hinzufügen, um Filter anzuwenden
                            setOnClickListener {
                                val currentSelectedKeywords = viewModel.selectedKeywords.value?.toMutableList() ?: mutableListOf()

                                // Toggle-Logik für die Schlüsselwörter
                                if (currentSelectedKeywords.contains(keyword)) {
                                    currentSelectedKeywords.remove(keyword)
                                } else {
                                    currentSelectedKeywords.add(keyword)
                                }

                                viewModel.filterByKeyword(currentSelectedKeywords) // Aktualisiere die ausgewählten Schlüsselwörter
                            }
                        }
                        binding.filterContainer.addView(textView)
                    }
                }
            } ?: run {
                Snackbar.make(requireView(), "Fehler beim Laden der Schlüsselwörter", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeSearchBar() {
        // Adapter für Vorschläge initialisieren
        suggestionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.searchEditText.setAdapter(suggestionAdapter) // Autocomplete-Adapter zu AutoCompleteTextView hinzufügen

        // Listener für die Suchleiste hinzufügen
        binding.searchEditText.addTextChangedListener { editable ->
            val searchText = editable.toString()
            handleSearchTextChanged(searchText)
        }
    }

    private fun handleSearchTextChanged(searchText: String) {
        if (searchText.isEmpty()) {
            viewModel.loadAllQuotes() // Alle Zitate laden, wenn die Suchleiste leer ist
            suggestionAdapter.clear() // Vorschläge zurücksetzen
        } else {
            // Vorschläge basierend auf dem Suchtext aktualisieren
            updateSuggestions(searchText)

            // Autorenvorschläge aktualisieren
            viewModel.updateSuggestedAuthors(searchText)

            // Suche nach Autor und Keywords
            viewModel.searchByAuthorAndKeywords(searchText, listOf(searchText)) // Hier wird eine Liste mit einem Keyword erstellt
        }
    }

    private fun updateSuggestions(query: String) {
        val filteredAuthors = viewModel.authors.value?.filter { it.name.contains(query, ignoreCase = true) }
        val filteredKeywords = viewModel.availableKeywords.value?.filter { it.contains(query, ignoreCase = true) }

        val suggestions = (filteredAuthors?.map { it.name } ?: emptyList()).union(filteredKeywords ?: emptyList()).toList()
        suggestionAdapter.clear()
        suggestionAdapter.addAll(suggestions)
        suggestionAdapter.notifyDataSetChanged() // Adapter benachrichtigen, dass die Daten aktualisiert wurden
    }

    private fun setupLoadingAndErrorHandling() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveQuote(quote: Quote) {
        viewModel.saveQuote(quote) // Speichern des Zitats im ViewModel
        Toast.makeText(requireContext(), "Zitat gespeichert.", Toast.LENGTH_SHORT).show()
    }
}