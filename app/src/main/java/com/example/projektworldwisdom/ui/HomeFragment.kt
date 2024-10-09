package com.example.projektworldwisdom.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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


        quotesAdapter = QuotesAdapter(emptyList(), sharedViewModel) { quote ->
            // Navigiere zu den Details des Autors
            navigateToAuthorDetails(quote)
        }
        binding.quotesList.adapter = quotesAdapter

        // Beobachte die Zitate
        sharedViewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            quotesAdapter.updateQuotes(quotes) // Liste im Adapter aktualisieren
        }

        // Beobachte die gefilterten Zitate
        sharedViewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            quotesAdapter.updateQuotes(filteredQuotes) // Gefilterte Liste im Adapter aktualisieren
        }

        // Zitate abrufen
        sharedViewModel.getQuotes()


        // Filter für Kategorien
        binding.filterChange.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Veränderung").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterFrieden.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Frieden").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterErfolg.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Erfolg").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterMotivation.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Motivation").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterHappy.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Glück").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterIndividuality.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Individualität").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterCreativity.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Kreativität").observe(viewLifecycleOwner) { filteredQuotes ->
                // Aktualisiere die RecyclerView mit den gefilterten Zitaten
                quotesAdapter.updateQuotes(filteredQuotes)
            }
        }

        binding.filterAll.setOnClickListener {

            sharedViewModel.getQuotes() // Alle Zitate abrufen
        }

        // Zitat des Tages abrufen und anzeigen
        fetchAndDisplayQuoteOfTheDay()

        // ImageButton für das neue Zitat laden
        binding.refreshButton.setOnClickListener {
            sharedViewModel.fetchQuoteOfTheDay() // Neues Zitat des Tages abrufen
            fetchAndDisplayQuoteOfTheDay() // Zitat des Tages neu laden und anzeigen
        }
    }

    private fun fetchAndDisplayQuoteOfTheDay() {
        // Beobachte das Zitat des Tages
        sharedViewModel.quoteOfTheDay.observe(viewLifecycleOwner) { quote ->
            quote?.let {
                binding.affirmationText.text = it.content
                binding.affirmationAuthor.text = it.author.name
            }
        }
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        sharedViewModel.selectAuthor(quote.author)
        sharedViewModel.selectQuote(quote)

        // Logik für die Navigation zu den Details des Autors
        val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment()
        findNavController().navigate(action)
    }

}