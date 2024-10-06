package com.example.projektworldwisdom.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.adapter.QuotesAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val sharedViewModel: SharedViewModel by activityViewModels() // SharedViewModel-Referenz
    private lateinit var quotesAdapter: QuotesAdapter // Adapter-Referenz

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter initialisieren (nur einmal)
        quotesAdapter = QuotesAdapter(emptyList(), sharedViewModel, { quote ->
            // Navigiere zu den Details des Autors
            navigateToAuthorDetails(quote)
        }, { quote ->
            // Speichere die Quote
            saveQuote(quote)
        })
        binding.quotesList.adapter = quotesAdapter

        // Beobachte die Zitate
        sharedViewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            quotesAdapter.updateQuotes(quotes) // Liste im Adapter aktualisieren
        }

        // Beobachte die gefilterten Zitate
        sharedViewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            quotesAdapter.updateQuotes(filteredQuotes) // Gefilterte Liste im Adapter aktualisieren
        }

        // Beobachte das ausgewählte Zitat
        sharedViewModel.selectedQuote.observe(viewLifecycleOwner) { quote ->
            quote?.let {

            }
        }

        // Zitate abrufen
        sharedViewModel.getQuotes()

        // Filter-Input für die SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtere die Zitate basierend auf dem Autorennamen
                sharedViewModel.filterQuotesForHome(newText ?: "")
                return true
            }
        })
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        sharedViewModel.selectAuthor(quote.author) // Übergib das gesamte Author-Objekt
        sharedViewModel.selectQuote(quote) // Wähle das Zitat aus

        // Logik für die Navigation zu den Details des Autors
        val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment()
        findNavController().navigate(action)
    }

    private fun saveQuote(quote: Quote) {
        sharedViewModel.saveQuote(quote) // Speicher die Quote über das SharedViewModel
    }
}