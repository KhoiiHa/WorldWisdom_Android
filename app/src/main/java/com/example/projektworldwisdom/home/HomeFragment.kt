package com.example.projektworldwisdom.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlin.text.Typography.quote

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModels<HomeViewModel> {
        val apiService = WorldWisdomApi.retrofitService
        val database = QuoteDatabase.getDatabase(requireContext())
        val quoteDao = database.quoteDao()
        val repository = QuoteRepository(quoteDao, apiService)
        HomeViewModelFactory(repository)
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var quoteAdapter: QuoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()  // Initialisierung der RecyclerView
        setupObservers()     // Einrichten der Observer für LiveData
        setupClickListeners() // Einrichten der Klick-Listener für Filter
        setupSearchView()    // Einrichten der Suchleiste
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(emptyList(), emptyList())

        binding.quotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }

        // Klick-Listener für einzelne Zitate
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorName = quote.authorName ?: "Unbekannter Autor"
                Log.d("HomeFragment", "Navigating to AuthorDetailsFragment with Author: $authorName, Quote: ${quote.content}")

                // Navigiere zu AuthorDetailsFragment
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(authorName, quote)
                )
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        var cachedQuotes: List<Quote>? = null

        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            Log.d("HomeFragment", "Received quotes: $quotes")
            cachedQuotes = quotes // Zitate zwischenspeichern

            viewModel.loadAllAuthors() // Autoren laden
        }

        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            Log.d("HomeFragment", "Received authors: $authors")

            if (cachedQuotes.isNullOrEmpty() || authors.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Keine Zitate oder Autoren gefunden.", Toast.LENGTH_SHORT).show()
            } else {
                quoteAdapter.updateData(cachedQuotes ?: emptyList(), authors ?: emptyList())
                Log.d("HomeFragment", "Updated adapter with quotes and authors")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehler zurücksetzen
            }
        }

        viewModel.dailyAffirmation.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                binding.affirmationText.text = quote.content
                val authorName = quote.authorName ?: "- Unbekannter Autor"
                binding.affirmationAuthor.text = authorName

                if (authorName.isNotBlank()) {
                    viewModel.getAuthorByName(authorName).observe(viewLifecycleOwner) { author ->
                        binding.affirmationAuthor.text = author?.name ?: "- Unbekannter Autor"
                    }
                }
            } else {
                binding.affirmationText.text = "Keine Zitate des Tages gefunden."
                binding.affirmationAuthor.text = "- Unbekannter Autor"
            }
        }
    }

    private fun setupClickListeners() {
        val keywordClickListener = View.OnClickListener { view ->
            val keywords = mutableListOf<String>()

            when (view.id) {
                R.id.filter_society -> keywords.add("Gesellschaft")
                R.id.filter_success -> keywords.add("Erfolg")
                R.id.filter_work -> keywords.add("Arbeit")
                R.id.filter_wisdom -> keywords.add("Weisheit")
                R.id.filter_gratitude -> keywords.add("Dankbarkeit")
                R.id.filter_all -> {
                    viewModel.loadAllQuotesHome()
                    return@OnClickListener
                }
                else -> return@OnClickListener
            }

            if (keywords.isNotEmpty()) {
                viewModel.loadQuotesByKeywords(keywords)
            }
        }

        binding.filterSociety.setOnClickListener(keywordClickListener)
        binding.filterSuccess.setOnClickListener(keywordClickListener)
        binding.filterWork.setOnClickListener(keywordClickListener)
        binding.filterWisdom.setOnClickListener(keywordClickListener)
        binding.filterGratitude.setOnClickListener(keywordClickListener)
        binding.filterAll.setOnClickListener { viewModel.loadAllQuotesHome() }
    }

    // Neues Setup für die Suchleiste
    private fun setupSearchView() {
        val searchView = binding.searchBar // Angenommene ID deiner Suchleiste
        val suggestions = mutableListOf<String>()

        // Lade Autoren und Zitate für die Vorschläge
        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            if (authors != null) {
                suggestions.addAll(authors.map { it.name })
            }
            if (authors != null) {
                suggestions.addAll(authors.map { it.tag })
            }
            suggestions.addAll(viewModel.quotes.value?.flatMap { it.keywords } ?: emptyList())

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
            searchView.setAdapter(adapter)

            searchView.setOnItemClickListener { parent, view, position, id ->
                val selectedOption = adapter.getItem(position)
                handleSearchSelection(selectedOption)
            }
        }
    }

    // Funktion zur Verarbeitung der Suchauswahl
    private fun handleSearchSelection(selectedOption: String?) {
        when {
            selectedOption != null -> {
                // Überprüfe, ob es ein Autor ist
                val author = viewModel.authors.value?.find { it.name == selectedOption }
                author?.let {
                    // Navigiere zum AuthorDetailsFragment mit dem Autorennamen und einem Beispiel-Zitat
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(
                            it.name,
                            Quote(authorName = it.name, content = "Beispiel-Zitat") // Beispiel-Zitat hier einfügen
                        )
                    )
                    return // Beende die Funktion nach der Navigation
                }

                // Überprüfe auf Tags
                val tag = viewModel.authors.value?.find { it.tag == selectedOption }
                tag?.let {
                    // Navigiere zur Suche mit dem Tag
                    viewModel.searchByTag(selectedOption) // Suche nach Zitaten mit dem Tag
                    // Hier könnte auch eine Rückkehr oder zusätzliche Logik hinzugefügt werden
                }

                // Überprüfe auf Keywords
                val keyword = viewModel.quotes.value?.flatMap { it.keywords }?.find { it == selectedOption }
                if (keyword != null) {
                    // Navigiere zur Suche mit dem Keyword
                    viewModel.searchByKeyword(keyword) // Suche nach Zitaten mit dem Keyword
                }
            }
        }
    }

}