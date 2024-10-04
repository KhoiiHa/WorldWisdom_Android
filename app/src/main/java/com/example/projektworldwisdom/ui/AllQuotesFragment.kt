package com.example.projektworldwisdom.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.adapter.QuotesAdapter
import com.example.projektworldwisdom.databinding.FragmentAllQuotesBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.AllQuotesViewModel
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class AllQuotesFragment : Fragment() {
    private lateinit var binding: FragmentAllQuotesBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: QuotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter initialisieren und an die RecyclerView binden
        adapter = QuotesAdapter(emptyList(), sharedViewModel, { quote ->
            navigateToAuthorDetails(quote)
        }, { quote ->
            saveQuote(quote)
        })
        binding.allQuotesList.adapter = adapter

        // Zitate abrufen
        sharedViewModel.getQuotes()

        // Beobachte die Zitate
        sharedViewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            adapter.updateQuotes(quotes) // Setze alle Zitate
        }

        // Beobachte die gefilterten Zitate
        sharedViewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            adapter.updateQuotes(filteredQuotes) // Aktualisiere den Adapter mit gefilterten Zitaten
        }

        // Suchleiste
        binding.searchEditText.addTextChangedListener { text ->
            val searchQuery = text.toString()
            sharedViewModel.filterQuotesForAll(searchQuery) // Aufruf der Filtermethode im SharedViewModel
        }
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        // Setze den ausgewählten Autor im SharedViewModel
        sharedViewModel.selectAuthor(quote.author) // Sicherstellen, dass der Autor übergeben wird
        val action = AllQuotesFragmentDirections.actionAllQuotesFragmentToAuthorDetailsFragment()
        findNavController().navigate(action)
    }

    private fun saveQuote(quote: Quote) {
        sharedViewModel.saveQuote(quote) // Zitat speichern
        // Optionale Rückmeldung hinzufügen
        Toast.makeText(requireContext(), "Zitat gespeichert!", Toast.LENGTH_SHORT).show()
    }
}