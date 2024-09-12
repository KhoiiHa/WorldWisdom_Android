package com.example.projektworldwisdom.allquotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.QuoteAdapter
import com.example.projektworldwisdom.databinding.FragmentAllQuotesBinding
import com.example.projektworldwisdom.home.HomeViewModel

class AllQuotesFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentAllQuotesBinding
    private lateinit var quoteAdapter: QuoteAdapter

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

        // RecyclerView einrichten
        quoteAdapter = QuoteAdapter(emptyList())
        binding.allQuotesList?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = quoteAdapter
        }

        // LiveData beobachten und Adapter aktualisieren
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            if (quotes != null) {
                quoteAdapter.updateData(quotes)
            }
        }

        // Lade alle Zitate
        viewModel.loadAllQuotes()
    }

}