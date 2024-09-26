package com.example.projektworldwisdom.allquotes


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val viewModel: AllQuotesViewModel by viewModels {
        val apiService = WorldWisdomApi.retrofitService
        val database = QuoteDatabase.getDatabase(requireContext())
        val quoteDao = database.quoteDao()
        val repository = QuoteRepository(quoteDao, apiService)
        AllQuotesViewModelFactory(repository)
    }

    private lateinit var binding: FragmentAllQuotesBinding
    private lateinit var quoteAdapter: QuoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView einrichten
        quoteAdapter = QuoteAdapter(emptyList(), emptyList())
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                quote.authorName?.let { authorName ->
                    findNavController().navigate(
                        AllQuotesFragmentDirections.actionAllQuotesFragmentToAuthorDetailsFragment(authorName, quote)
                    )
                } ?: run {
                    Toast.makeText(context, "Autorname nicht verfügbar", Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.allQuotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }

        // LiveData beobachten und Adapter aktualisieren
        viewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            if (filteredQuotes != null) {
                quoteAdapter.updateData(filteredQuotes, viewModel.authors.value ?: emptyList())
            } else {
                Snackbar.make(view, "Fehler beim Laden der gefilterten Zitate", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Beobachtung der verfügbaren Schlüsselwörter
        viewModel.availableKeywords.observe(viewLifecycleOwner) { keywords ->
            binding.filterContainer.removeAllViews() // Vorherige Filter-Views entfernen

            keywords?.let {
                it.forEach { keyword ->
                    val textView = TextView(requireContext()).apply {
                        text = keyword
                        setPadding(16, 8, 16, 8)

                        // OnClickListener hinzufügen, um Filter anzuwenden
                        setOnClickListener {
                            val currentSelectedKeywords = viewModel.selectedKeywords.value?.toMutableList() ?: mutableListOf()

                            if (currentSelectedKeywords.contains(keyword)) {
                                currentSelectedKeywords.remove(keyword) // Entferne das Keyword, wenn es bereits ausgewählt ist
                            } else {
                                currentSelectedKeywords.add(keyword) // Füge das Keyword hinzu, wenn es nicht ausgewählt ist
                            }

                            viewModel.filterByKeyword(currentSelectedKeywords) // Aktualisiere die ausgewählten Schlüsselwörter
                        }
                    }
                    binding.filterContainer.addView(textView)
                }
            } ?: run {
                Snackbar.make(view, "Keine Schlüsselwörter verfügbar", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Suchfunktion verknüpfen
        binding.searchEditText.addTextChangedListener { editable ->
            val searchText = editable.toString()
            if (searchText.isEmpty()) {
                viewModel.loadAllQuotes() // Alle Zitate laden, wenn die Suchleiste leer ist
            } else {
                viewModel.searchByAuthor(searchText)
            }
        }

        // Ladeanzeige und Fehlerbehandlung
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(view, errorMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}