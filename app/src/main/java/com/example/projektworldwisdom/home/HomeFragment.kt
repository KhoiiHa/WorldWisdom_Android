package com.example.projektworldwisdom.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlin.text.Typography.quote

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var quoteAdapter: QuoteAdapter
    private val viewModel: HomeViewModel by activityViewModels<HomeViewModel> {
        HomeViewModelFactory(
            QuoteRepository(
                QuoteDatabase.getDatabase(requireContext()).quoteDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSearchView() // Setup der Suchleiste
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(emptyList(), emptyList()) { quote ->
            saveQuote(quote) // Speichern beim Klick auf den Speichern-Button
        }

        binding.quotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }

        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorName = quote.authorName ?: "Unbekannter Autor"
                Log.d(
                    "HomeFragment",
                    "Navigating to AuthorDetailsFragment with Author: $authorName, Quote: ${quote.content}"
                )

                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(
                        authorName,
                        quote
                    )
                )
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            Log.d("HomeFragment", "Received quotes: $quotes")
            quotes?.let {
                quoteAdapter.updateData(it, viewModel.authors.value ?: emptyList())
            } ?: Toast.makeText(requireContext(), "Keine Zitate gefunden.", Toast.LENGTH_SHORT)
                .show()
        }

        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            Log.d("HomeFragment", "Received authors: $authors")
            authors?.let {
                quoteAdapter.updateData(viewModel.quotes.value ?: emptyList(), it)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.dailyAffirmation.observe(viewLifecycleOwner) { quote ->
            binding.affirmationText.text = quote?.content ?: "Keine Zitate des Tages gefunden."
            binding.affirmationAuthor.text = quote?.authorName ?: "- Unbekannter Autor"
        }
    }

    private fun setupClickListeners() {
        val keywordClickListener = View.OnClickListener { view ->
            val keywords = when (view.id) {
                R.id.filter_society -> listOf("Gesellschaft")
                R.id.filter_success -> listOf("Erfolg")
                R.id.filter_work -> listOf("Arbeit")
                R.id.filter_wisdom -> listOf("Weisheit")
                R.id.filter_gratitude -> listOf("Dankbarkeit")
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

        binding.apply {
            filterSociety.setOnClickListener(keywordClickListener)
            filterSuccess.setOnClickListener(keywordClickListener)
            filterWork.setOnClickListener(keywordClickListener)
            filterWisdom.setOnClickListener(keywordClickListener)
            filterGratitude.setOnClickListener(keywordClickListener)
            filterAll.setOnClickListener { viewModel.loadAllQuotesHome() }
        }
    }

    private fun setupSearchView() {
        val searchView = binding.searchBar

        // Adapter für die Suchleiste, der mit einer leeren Liste initialisiert wird
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf() // Start mit einer leeren Liste
        )
        searchView.setAdapter(adapter)

        // Setze den Listener für die Auswahl eines Vorschlags
        searchView.setOnItemClickListener { parent, view, position, id ->
            val selectedOption = adapter.getItem(position)
            // Verarbeite die Auswahl des Vorschlags
            handleSearchSelection(selectedOption)
        }

        // Beobachte die Autocomplete-Vorschläge aus dem ViewModel
        viewModel.autocompleteSuggestions.observe(viewLifecycleOwner) { suggestions ->
            suggestions?.let {
                // Aktualisiere den Adapter mit den neuen Vorschlägen
                adapter.clear() // Leere die aktuelle Liste im Adapter
                adapter.addAll(it) // Füge die neuen Vorschläge hinzu
                adapter.notifyDataSetChanged() // Informiere den Adapter über die Änderungen
            }
        }

        // TextWatcher für die Suchleiste, um Autocomplete-Vorschläge zu aktualisieren
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchAutocomplete(s.toString()) // Aktualisiere die Vorschläge basierend auf der Eingabe
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun handleSearchSelection(selectedOption: String?) {
        if (selectedOption == null) return

        val keywords = getKeywordsFromInput()

        // Zuerst versuche den Autor anhand des ausgewählten Namens abzurufen
        viewModel.getAuthorByName(selectedOption).observe(viewLifecycleOwner) { author ->
            author?.let {
                // Wenn der Autor gefunden wurde, navigiere zu den Details
                navigateToAuthorDetails(it)
            } ?: run {
                // Wenn der Autor nicht gefunden wurde, überprüfe, ob es ein Tag oder ein Keyword ist
                viewModel.authors.value?.find { it.name == selectedOption }?.let { author ->
                    // Suche nach Zitaten basierend auf dem Autorennamen, Keywords und Tag
                    viewModel.searchByAuthorAndKeywordsAndTags(author.name, keywords, author.tag)
                    navigateToAuthorDetails(author)
                } ?: viewModel.authors.value?.find { it.tag == selectedOption }?.let { tag ->
                    // Suche nach Zitaten basierend auf dem Tag
                    viewModel.searchByTag(tag.toString())
                } ?: run {
                    // Fallback für die Suche nach Zitaten, wenn kein Autor oder Tag gefunden wurde
                    viewModel.searchQuotes(selectedOption, keywords, null) // Suche nach Zitaten basierend auf dem Titel und den Keywords
                }
            }
        }
    }

    private fun navigateToAuthorDetails(author: Author) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(
                author.name,
                Quote(
                    authorName = author.name,
                    content = "Beispiel-Zitat" // Hier kannst du das echte Zitat einsetzen
                )
            )
        )
    }

    private fun getKeywordsFromInput(): List<String> {
        return binding.searchBar.text.toString()
            .split(" ")
            .filter { it.isNotEmpty() }
            .map { it.trim() }
    }

    private fun saveQuote(quote: Quote) {
        viewModel.saveQuote(quote) // Speichern des Zitats im ViewModel
        Toast.makeText(requireContext(), "Zitat gespeichert.", Toast.LENGTH_SHORT).show()
    }
}