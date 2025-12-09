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
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var quotesAdapter: QuoteAdapter

    private var allQuotes: List<Quote> = emptyList()
    private var currentCategory: String? = null
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Zitate laden
        viewModel.loadQuotes()

        // Ladeanzeige
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // RecyclerView + Adapter
        quotesAdapter = QuoteAdapter(emptyList()).apply {
            setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
                override fun onItemClick(quote: Quote) {
                    // Navigation: wir nehmen den Autor-Namen
                    val action = HomeFragmentDirections
                        .actionHomeFragmentToAuthorDetailsFragment(quote.author)
                    findNavController().navigate(action)
                }
            })
        }

        binding.quotesList.layoutManager = LinearLayoutManager(requireContext())
        binding.quotesList.adapter = quotesAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Zitate-Liste beobachten
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            allQuotes = quotes ?: emptyList()
            applyFilters()
        }

        // Suchfeld
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // nicht benötigt
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                currentSearchQuery = s?.toString().orEmpty()
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {
                // nicht benötigt
            }
        })

        // Kategorie-Filter
        binding.filterSociety.setOnClickListener {
            currentCategory = "society"
            applyFilters()
        }
        binding.filterSuccess.setOnClickListener {
            currentCategory = "success"
            applyFilters()
        }
        binding.filterWork.setOnClickListener {
            currentCategory = "work"
            applyFilters()
        }
        binding.filterWisdom.setOnClickListener {
            currentCategory = "wisdom"
            applyFilters()
        }
        binding.filterGratitude.setOnClickListener {
            currentCategory = "gratitude"
            applyFilters()
        }
        binding.filterAlle.setOnClickListener {
            currentCategory = null
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

    private fun applyFilters() {
        val search = currentSearchQuery.trim()
        val category = currentCategory

        val filtered = allQuotes
            .filter { quote ->
                category == null ||
                        quote.category.equals(category, ignoreCase = true)
            }
            .filter { quote ->
                if (search.isEmpty()) {
                    true
                } else {
                    quote.quote.contains(search, ignoreCase = true) ||
                            quote.author.contains(search, ignoreCase = true)
                }
            }

        quotesAdapter.updateQuotes(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}