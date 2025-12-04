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
import com.example.projektworldwisdom.adapter.QuotesAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var quotesAdapter: QuotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Alle Zitate beim Start laden
        viewModel.loadQuotes()

        // Ladeanzeige
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // RecyclerView + Adapter
        quotesAdapter = QuotesAdapter(emptyList()).apply {
            setOnItemClickListener(object : QuotesAdapter.OnItemClickListener {
                override fun onItemClick(quote: Quote) {
                    val action = HomeFragmentDirections
                        .actionHomeFragmentToAuthorDetailsFragment(quote.authorSlug)
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

        // LiveData beobachten und Adapter updaten
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            quotesAdapter.updateQuotes(quotes)
        }

        // Suche
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
                viewModel.searchQuotes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // nicht benötigt
            }
        })

        // Filter-Buttons
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
            viewModel.loadQuotesByTag("Alle")
        }

        // Fehlerbehandlung
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}