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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentAllQuotesBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
import com.google.android.material.snackbar.Snackbar

class AllQuotesFragment : Fragment() {

    private val viewModel: AllQuotesViewModel by activityViewModels()


    private lateinit var binding: FragmentAllQuotesBinding
    private lateinit var quoteAdapter: QuoteAdapter
    private lateinit var suggestionAdapter: ArrayAdapter<String>

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

        viewModel.loadAvailableKeywords()
        setupRecyclerView()
        observeLiveData()
        observeAvailableKeywords()
        initializeSearchBar()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(emptyList(), emptyList(), viewModel::saveQuote)
        binding.allQuotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }

        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorName = quote.authorName ?: "Unbekannter Autor"
                Log.d("AllQuotesFragment", "Navigating to AuthorDetailsFragment with Author: $authorName, Quote: ${quote.content}")
                viewModel.setQuote(quote)
                // Hier wird die Navigation zur AuthorDetailsFragment mit den erforderlichen Argumenten durchgeführt
                findNavController().navigate(
                    AllQuotesFragmentDirections.actionAllQuotesFragmentToAuthorDetailsFragment(authorName, quote) // Das Zitat wird hier übergeben)
                )
            }
        })

        quoteAdapter.setOnSaveClickListener { quote ->
            saveQuote(quote)
        }
    }

    private fun observeLiveData() {
        viewModel.filteredQuotes.observe(viewLifecycleOwner) { filteredQuotes ->
            filteredQuotes?.let {
                Log.d("FilteredQuotes", "Number of filtered quotes: ${it.size}")
                quoteAdapter.updateData(it, viewModel.authors.value ?: emptyList())
            } ?: showSnackbar("Fehler beim Laden der gefilterten Zitate")
        }
    }


    private fun observeAvailableKeywords() {
        viewModel.availableKeywords.observe(viewLifecycleOwner) { keywords ->
            binding.filterContainer.removeAllViews()

            keywords?.let { keywordList ->
                binding.filterContainer.visibility = if (keywordList.isNotEmpty()) View.VISIBLE else View.GONE

                keywordList.forEach { keyword ->
                    val textView = createFilterTextView(keyword)
                    binding.filterContainer.addView(textView)
                }
            } ?: run {
                showSnackbar("Fehler beim Laden der Schlüsselwörter")
                Log.e("KeywordError", "Keywords are null or empty")
            }
        }
    }

    private fun createFilterTextView(keyword: String): TextView {
        return TextView(requireContext()).apply {
            text = keyword
            setPadding(16, 8, 16, 8)
            setOnClickListener {
                toggleKeywordSelection(keyword)
            }
        }
    }

    private fun toggleKeywordSelection(keyword: String) {
        val currentSelectedKeywords = viewModel.selectedKeywords.value?.toMutableList() ?: mutableListOf()

        if (currentSelectedKeywords.contains(keyword)) {
            currentSelectedKeywords.remove(keyword)
        } else {
            currentSelectedKeywords.add(keyword)
        }

        viewModel.updateSelectedKeywords(currentSelectedKeywords)
        viewModel.filterByKeyword(currentSelectedKeywords)
    }

    private fun initializeSearchBar() {
        suggestionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.searchEditText.setAdapter(suggestionAdapter)

        binding.searchEditText.addTextChangedListener { editable ->
            val searchText = editable.toString()
            handleSearchTextChanged(searchText)
            updateSuggestions(searchText)
        }
    }

    private fun handleSearchTextChanged(searchText: String) {
        Log.d("SearchText", "Input: $searchText")

        if (searchText.isEmpty()) {
            viewModel.loadAllQuotes()
        } else {
            val filteredAuthors = viewModel.authors.value?.filter { it.name.contains(searchText, ignoreCase = true) }
            if (!filteredAuthors.isNullOrEmpty()) {
                viewModel.searchByAuthorAndKeywords(authorName = searchText, keywords = emptyList())
            } else {
                viewModel.searchByAuthorAndKeywords(authorName = null, keywords = listOf(searchText))
            }
        }
        updateSuggestions(searchText)
    }

    private fun updateSuggestions(query: String) {
        val filteredAuthors = viewModel.authors.value?.filter { it.name.contains(query, ignoreCase = true) }
        val filteredKeywords = viewModel.availableKeywords.value?.filter { it.contains(query, ignoreCase = true) }

        val suggestions = (filteredAuthors?.map { it.name } ?: emptyList())
            .union(filteredKeywords ?: emptyList())
            .toList()

        suggestionAdapter.clear()
        suggestionAdapter.addAll(suggestions)
        suggestionAdapter.notifyDataSetChanged()
    }

    private fun saveQuote(quote: Quote) {
        viewModel.saveQuote(quote)
        Toast.makeText(requireContext(), "Zitat gespeichert.", Toast.LENGTH_SHORT).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
}