package com.example.projektworldwisdom.allquotes


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentAllQuotesBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
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

        viewModel.loadAvailableKeywords() // Lade die verfügbaren Keywords

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

        // Hier wird ein OnItemClickListener übergeben
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
                // Standardmäßig die Filter-Container sichtbar machen
                binding.filterContainer.visibility = if (keywordList.isNotEmpty()) View.VISIBLE else View.GONE

                // Hier fügen wir die Keywords hinzu
                keywordList.forEach { keyword ->
                    val textView = TextView(requireContext()).apply {
                        text = keyword
                        setPadding(16, 8, 16, 8)

                        // OnClickListener hinzufügen, um Filter anzuwenden
                        setOnClickListener {
                            Log.d("FilterClick", "Keyword clicked: $keyword") // Log für Debugging
                            val currentSelectedKeywords = viewModel.selectedKeywords.value?.toMutableList() ?: mutableListOf()

                            // Toggle-Logik für die Schlüsselwörter
                            if (currentSelectedKeywords.contains(keyword)) {
                                currentSelectedKeywords.remove(keyword)
                                Log.d("FilterClick", "Removed keyword: $keyword")
                            } else {
                                currentSelectedKeywords.add(keyword)
                                Log.d("FilterClick", "Added keyword: $keyword")
                            }

                            // Aktualisiere die ausgewählten Schlüsselwörter und filtere die Zitate
                            viewModel.updateSelectedKeywords(currentSelectedKeywords)
                            viewModel.filterByKeyword(currentSelectedKeywords)
                        }
                    }
                    binding.filterContainer.addView(textView)
                }
            } ?: run {
                // Snackbar nur anzeigen, wenn keywords null sind
                Snackbar.make(requireView(), "Fehler beim Laden der Schlüsselwörter", Snackbar.LENGTH_SHORT).show()
                Log.e("KeywordError", "Keywords are null or empty")
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
        Log.d("SearchText", "Input: $searchText")

        if (searchText.isEmpty()) {
            viewModel.loadAllQuotes() // Alle Zitate laden, wenn die Suchleiste leer ist
            suggestionAdapter.clear() // Vorschläge zurücksetzen
            viewModel.clearSelectedKeywords() // Optional: Wähle alle Keywords ab, wenn die Suche leer ist
        } else {
            // Vorschläge basierend auf dem Suchtext aktualisieren
            updateSuggestions(searchText)

            // Hol die aktuellen ausgewählten Keywords oder setze eine leere Liste
            val selectedKeywords = viewModel.selectedKeywords.value ?: emptyList()
            Log.d("SearchText", "Selected Keywords: $selectedKeywords")

            // Verwendet die Methode searchByAuthorAndKeywords, um sowohl nach Autor als auch Keywords zu suchen
            viewModel.searchByAuthorAndKeywords(
                authorName = if (selectedKeywords.isEmpty()) searchText else null, // Autorensuche nur, wenn keine Keywords
                keywords = if (selectedKeywords.isNotEmpty()) selectedKeywords else listOf(searchText) // Keywords nur verwenden, wenn vorhanden, sonst den Suchtext
            )
        }
    }

    private fun updateSuggestions(query: String) {
        val filteredAuthors = viewModel.authors.value?.filter { it.name.contains(query, ignoreCase = true) }
        val filteredKeywords = viewModel.availableKeywords.value?.filter { it.contains(query, ignoreCase = true) }

        val suggestions = (filteredAuthors?.map { it.name } ?: emptyList())
            .union(filteredKeywords ?: emptyList())
            .toList()

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