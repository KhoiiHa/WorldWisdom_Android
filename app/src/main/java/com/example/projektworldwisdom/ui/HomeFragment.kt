package com.example.projektworldwisdom.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.QuotesAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class HomeFragment : Fragment() {

    // ViewBinding mit nullable Pattern, um Memory-Leaks zu vermeiden
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var quotesAdapter: QuotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupQuotesList()
        observeQuotes()
        setupCategoryFilters()
        setupQuoteOfTheDay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setup-Methoden -------------------------------------------------------

    private fun setupQuotesList() {
        quotesAdapter = QuotesAdapter(emptyList(), sharedViewModel) { quote ->
            navigateToAuthorDetails(quote)
        }

        binding.quotesList.apply {
            adapter = quotesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Initial alle Zitate laden
        sharedViewModel.getQuotes()
    }

    private fun observeQuotes() {
        // komplette Zitate-Liste
        sharedViewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            quotesAdapter.updateQuotes(quotes)
        }

        // gefilterte Zitate-Liste
        sharedViewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            quotesAdapter.updateQuotes(filteredQuotes)
        }
    }

    private fun setupCategoryFilters() {
        binding.filterChange.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Veränderung")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterFrieden.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Frieden")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterErfolg.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Erfolg")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterMotivation.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Motivation")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterHappy.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Glück")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterIndividuality.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Individualität")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterCreativity.setOnClickListener {
            sharedViewModel.filterQuotesByCategory("Kreativität")
                .observe(viewLifecycleOwner) { filteredQuotes ->
                    quotesAdapter.updateQuotes(filteredQuotes)
                }
        }

        binding.filterAll.setOnClickListener {
            // Filter zurücksetzen → alle Zitate abrufen
            sharedViewModel.getQuotes()
        }
    }

    private fun setupQuoteOfTheDay() {
        // Quote of the Day einmalig beobachten
        sharedViewModel.quoteOfTheDay.observe(viewLifecycleOwner) { quote ->
            quote?.let {
                binding.affirmationText.text = it.content
                binding.affirmationAuthor.text = it.author.name
            }
        }

        // initial laden
        sharedViewModel.fetchQuoteOfTheDay()

        // bei Klick nur neuen Wert laden – Observer bleibt derselbe
        binding.refreshButton.setOnClickListener {
            sharedViewModel.fetchQuoteOfTheDay()
        }
    }

    // --- Navigation ------------------------------------------------------------

    private fun navigateToAuthorDetails(quote: Quote) {
        sharedViewModel.selectAuthor(quote.author)
        sharedViewModel.selectQuote(quote)

        val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment()
        findNavController().navigate(action)
    }
}