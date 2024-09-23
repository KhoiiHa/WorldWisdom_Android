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
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi

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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(emptyList())
        binding.quotesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quoteAdapter
        }

        // Klick-Listener für einzelne Zitate
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorName = quote.authorName
                if (authorName != null) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(authorName)
                    )
                } else {
                    // Behandlung, wenn authorName null ist
                    Toast.makeText(context, "Autorname nicht verfügbar", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            Log.d("Fragment", "Received quotes: $quotes")
            if (quotes.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Keine Zitate gefunden.", Toast.LENGTH_SHORT).show()
            } else {
                quoteAdapter.updateData(quotes)
                Log.d("Fragment", "Updated adapter with quotes: $quotes")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehler nach der Anzeige löschen
            }
        }


        viewModel.dailyAffirmation.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                // Setze den Zitattext
                binding.affirmationText.text = quote.content

                // Hole den Autorennamen nur, wenn er nicht null oder leer ist
                val authorName = quote.authorName
                if (!authorName.isNullOrEmpty()) {
                    viewModel.getAuthorByName(authorName).observe(viewLifecycleOwner) { author ->
                        binding.affirmationAuthor.text = author ?: "- Unbekannter Autor"
                    }
                } else {
                    binding.affirmationAuthor.text = "- Unbekannter Autor"
                }
            } else {
                // Kein Zitat des Tages gefunden
                binding.affirmationText.text = "Keine Zitate des Tages gefunden."
                binding.affirmationAuthor.text = "- Unbekannter Autor" // Konsistent bleiben
            }
        }

//        viewModel.keywords.observe(viewLifecycleOwner) { keywords ->
//            // Hier die Keywords in einer Spinner oder Dropdown-Liste anzeigen
//            // Beispiel:
//            val keywordList = keywords.map { it.keyword } // Assuming each Keyword has a 'keyword' property
//            // Update UI, um die Keywords darzustellen
//            // z.B. mit einem Spinner:
//            // binding.keywordSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, keywordList)
//        }
    }

    private fun setupClickListeners() {
        binding.filterSociety.setOnClickListener {
            viewModel.loadQuotesByKeyword("Gesellschaft")
        }

        binding.filterSuccess.setOnClickListener {
            viewModel.loadQuotesByKeyword("Erfolg")
        }

        binding.filterWork.setOnClickListener {
            viewModel.loadQuotesByKeyword("Arbeit")
        }

        binding.filterWisdom.setOnClickListener {
            viewModel.loadQuotesByKeyword("Weisheit")
        }

        binding.filterGratitude.setOnClickListener {
            viewModel.loadQuotesByKeyword("Dankbarkeit")
        }

        // Klick-Listener für alle Zitate
        binding.filterAlle.setOnClickListener {
            viewModel.loadAllQuotesHome()
        }

        // Optional: Klick-Listener für Schlüsselwörter
        // binding.loadKeywordsButton.setOnClickListener {
        //     viewModel.loadKeywords()
        // }
    }
}