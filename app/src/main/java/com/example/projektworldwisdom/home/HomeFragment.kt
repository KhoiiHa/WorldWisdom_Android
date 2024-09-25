package com.example.projektworldwisdom.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        // Initialisierung des ViewModels mit der Repository-Abhängigkeit
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
        // Inflating des Layouts für das Fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()  // Initialisierung der RecyclerView
        setupObservers()     // Einrichten der Observer für LiveData
        setupClickListeners() // Einrichten der Klick-Listener für Filter
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(emptyList()) // Adapter mit einer leeren Liste initialisieren
        binding.quotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }

        // Klick-Listener für einzelne Zitate
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorName = quote.authorName ?: "Unbekannter Autor"
                Log.d("HomeFragment", "Navigating to AuthorDetailsFragment with Author: $authorName, Quote: ${quote.content}")

                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(authorName, quote)
                )
            }
        })
    }

    private fun setupObservers() {
        // Observer für Ladezustand
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer für die Zitate
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            Log.d("Fragment", "Received quotes: $quotes")
            if (quotes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Keine Zitate gefunden.", Toast.LENGTH_SHORT).show()
            } else {
                quoteAdapter.updateData(quotes)
                Log.d("Fragment", "Updated adapter with quotes: $quotes")
            }
        }

        // Observer für Fehlernachrichten
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehler zurücksetzen
            }
        }

        // Observer für das Zitat des Tages
        viewModel.dailyAffirmation.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                binding.affirmationText.text = quote.content
                val authorName = quote.authorName ?: "- Unbekannter Autor"
                binding.affirmationAuthor.text = authorName

                // Autorinformationen abrufen
                if (authorName.isNotBlank()) {
                    viewModel.getAuthorByName(authorName).observe(viewLifecycleOwner) { author ->
                        binding.affirmationAuthor.text = author ?: "- Unbekannter Autor"
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
            val keywords = mutableListOf<String>() // Liste für die Keywords erstellen

            // Filter basierend auf dem angeklickten View
            when (view.id) {
                R.id.filter_society -> keywords.add("Gesellschaft")
                R.id.filter_success -> keywords.add("Erfolg")
                R.id.filter_work -> keywords.add("Arbeit")
                R.id.filter_wisdom -> keywords.add("Weisheit")
                R.id.filter_gratitude -> keywords.add("Dankbarkeit")
                R.id.filter_all -> {
                    viewModel.loadAllQuotesHome()
                    return@OnClickListener // Beende die Funktion hier
                }
                else -> return@OnClickListener
            }

            // Überprüfen, ob Keywords hinzugefügt wurden und die Zitate laden
            if (keywords.isNotEmpty()) {
                viewModel.loadQuotesByKeywords(keywords) // Verwende die Funktion für mehrere Keywords
            }
        }

        // Klick-Listener für die Filter-Buttons
        binding.filterSociety.setOnClickListener(keywordClickListener)
        binding.filterSuccess.setOnClickListener(keywordClickListener)
        binding.filterWork.setOnClickListener(keywordClickListener)
        binding.filterWisdom.setOnClickListener(keywordClickListener)
        binding.filterGratitude.setOnClickListener(keywordClickListener)
        binding.filterAll.setOnClickListener { viewModel.loadAllQuotesHome() }
    }
}