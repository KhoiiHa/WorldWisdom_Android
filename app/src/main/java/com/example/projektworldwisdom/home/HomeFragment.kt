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


class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var quoteAdapter: QuoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Ladeanzeige standardmäßig anzeigen
        binding.progressBar.visibility = View.VISIBLE

        // RecyclerView einrichten
        quoteAdapter = QuoteAdapter(emptyList())
        binding.quotesList?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = quoteAdapter
        }

        // LiveData beobachten und Adapter aktualisieren (in onCreateView verschoben)
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            quoteAdapter.updateData(quotes)
        }

        // Lade alle Zitate beim Start des Fragments
        viewModel.loadQuotes()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LiveData beobachten und CardView aktualisieren
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            if (quotes.isNotEmpty()) {
                val firstQuote = quotes[0]
                binding.affirmationText.text = firstQuote.content
                binding.affirmationAuthor.text = "- ${firstQuote.author}"
            } else {
                // Handle den Fall, dass keine Zitate geladen wurden (optional)
                binding.affirmationText.text = "Keine Zitate gefunden."
                binding.affirmationAuthor.text = ""
            }
        }


        // Klick-Listener für Filter
        binding.filterMotivation.setOnClickListener {
            viewModel.loadQuotesByTag("motivational")
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


    }
}