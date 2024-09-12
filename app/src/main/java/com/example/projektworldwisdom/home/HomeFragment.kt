package com.example.projektworldwisdom.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.local.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.remote.WorldWisdomApiService


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

        // Ladeanzeige standardmäßig anzeigen
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView einrichten
        quoteAdapter = QuoteAdapter(emptyList())
        binding.quotesList?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = quoteAdapter
        }

        // LiveData beobachten und CardView aktualisieren
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            if (quotes != null) {
                if (quotes.isNotEmpty()) {
                    val firstQuote = quotes[0]
                    binding.affirmationText.text = firstQuote.content
                    binding.affirmationAuthor.text = "- ${firstQuote.author}"
                } else {
                    // Handle den Fall, dass keine Zitate geladen wurden
                    binding.affirmationText.text = "Keine Zitate gefunden."
                    binding.affirmationAuthor.text = ""
                }
            }
        }

        // LiveData beobachten und RecyclerView aktualisieren
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            if (quotes != null) {
                quoteAdapter.updateData(quotes)
            }
        }

        // In HomeFragment, wenn der Benutzer auf ein Zitat klickt:
        quoteAdapter.setOnItemClickListener(object : QuoteAdapter.OnItemClickListener {
            override fun onItemClick(quote: Quote) {
                val authorSlug = quote.authorSlug // authorSlug aus dem Quote-Objekt holen
                val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailsFragment(authorSlug)
                findNavController().navigate(action)
            }
        })

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

        binding.filterAlle.setOnClickListener {
            viewModel.loadQuotes()
        }

        // Fehlerbehandlung
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel._error.postValue(null)
            }
        }

        // Lade alle Zitate beim Start des Fragments
        viewModel.loadQuotes()
    }
}