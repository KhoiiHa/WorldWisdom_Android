package com.example.projektworldwisdom.collections


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektworldwisdom.adapter.CollectionsAdapter
import com.example.projektworldwisdom.databinding.FragmentCollectionsBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.mockApi.MockApi
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import com.example.projektworldwisdom.repository.QuoteRepository
import com.google.android.material.snackbar.Snackbar

class CollectionsFragment : Fragment() {

    private lateinit var viewModel: CollectionsViewModel
    private lateinit var adapter: CollectionsAdapter
    private lateinit var binding: FragmentCollectionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisiere den ViewModel
        val repository =
            QuoteRepository(quoteDao = QuoteDatabase.getDatabase(requireContext()).quoteDao())
        viewModel = ViewModelProvider(this, CollectionsViewModelFactory(repository)).get(
            CollectionsViewModel::class.java
        )

        // Initialisiere den Adapter
        adapter = CollectionsAdapter(
            onCommentClick = { quote, comment ->
                viewModel.addCommentToQuote(quote, comment)
            },
            onDeleteClick = { quote ->
                viewModel.deleteQuote(quote)
            }
        )

        binding.recyclerViewCollections.adapter = adapter

        // Setze den LayoutManager für die RecyclerView
        binding.recyclerViewCollections.layoutManager = LinearLayoutManager(requireContext())

        viewModel.savedQuotes.observe(viewLifecycleOwner) { quotes ->
            Log.d("CollectionsFragment", "Gespeicherte Zitate: $quotes")
            adapter.updateData(quotes)
        }

        // Beobachte auf Fehler
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Kommentieren Button-Klick
        binding.saveCommentButton.setOnClickListener {
            val selectedQuote = adapter.getSelectedQuote()
            val commentText = binding.commentEditText.text.toString()
            if (selectedQuote != null) {
                viewModel.addCommentToQuote(selectedQuote, commentText)
                binding.commentEditText.text.clear() // Leere das Textfeld nach dem Speichern
            } else {
                Toast.makeText(requireContext(), "Bitte ein Zitat auswählen", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}