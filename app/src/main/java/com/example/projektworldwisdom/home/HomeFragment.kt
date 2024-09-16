package com.example.projektworldwisdom.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        // Lade das Zitat des Tages beim Start des Fragments
        viewModel.loadQuoteOfTheDay()
    }

    private fun setupRecyclerView() {
        // RecyclerView einrichten
        quoteAdapter = QuoteAdapter(emptyList())
        binding.quotesList?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = quoteAdapter
        }

        // Klick-Listener für Zitate
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                // Zeige das Zitat in einem Dialog oder neuen Fragment an, falls erforderlich
                Toast.makeText(requireContext(), "${quote.q} — ${quote.a}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun setupObservers() {
        // Ladeanzeige standardmäßig anzeigen
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // LiveData beobachten und UI aktualisieren
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            if (quotes != null) {
                if (quotes.isNotEmpty()) {
                    val firstQuote = quotes[0]
                    binding.affirmationText.text = firstQuote.q // Zitattext anzeigen
                    binding.affirmationAuthor.text = "- ${firstQuote.a}" // Autor anzeigen
                } else {
                    // Handle den Fall, dass keine Zitate geladen wurden
                    binding.affirmationText.text = "Keine Zitate gefunden."
                    binding.affirmationAuthor.text = ""
                }
                quoteAdapter.updateData(quotes)
            }
        }

        // Fehlerbehandlung
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehlermeldung auf null setzen
            }
        }

        // Zitat des Tages beobachten
        viewModel.affirmation.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                binding.affirmationText.text = quote.q
                binding.affirmationAuthor.text = "- ${quote.a}"
            } else {
                // Handle den Fall, dass kein Zitat des Tages geladen wurde
                binding.affirmationText.text = "Keine Zitate des Tages gefunden."
                binding.affirmationAuthor.text = ""
            }
        }

        // Schlüsselwörter beobachten
        viewModel.keywords.observe(viewLifecycleOwner) { keywords ->
            // Hier kannst du mit den Schlüsselwörtern arbeiten, z.B. in einer Dropdown-Liste anzeigen
            // Beispiel: Update einer Spinner oder einer anderen Ansicht mit den Keywords
        }
    }

    private fun setupClickListeners() {
        // Klick-Listener für Filter
        binding.filterSociety.setOnClickListener {
            viewModel.loadQuotesByTag("society")
        }

        binding.filterSuccess.setOnClickListener {
            viewModel.loadQuotesByTag("success")
        }

        binding.filterWork.setOnClickListener {
            viewModel.loadQuotesByTag("work")
        }

        binding.filterWisdom.setOnClickListener {
            viewModel.loadQuotesByTag("wisdom")
        }

        binding.filterGratitude.setOnClickListener {
            viewModel.loadQuotesByTag("gratitude")
        }

//        // Klick-Listener für alle Zitate
//        binding.filterAll.setOnClickListener {
//            viewModel.loadAllQuotesHome()
//        }
//
//        // Klick-Listener für Schlüsselwörter
//        binding.loadKeywordsButton.setOnClickListener {
//            viewModel.loadKeywords()
//        }
//    }
    }
}