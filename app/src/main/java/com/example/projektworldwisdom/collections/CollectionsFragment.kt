package com.example.projektworldwisdom.collections


import android.os.Bundle
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

    private var _binding: FragmentCollectionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CollectionsViewModel
    private lateinit var adapter: CollectionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Repository initialisieren
        val quoteDao = QuoteDatabase.getDatabase(requireContext()).quoteDao()
        val apiService = WorldWisdomApi.retrofitService
        val repository = QuoteRepository(quoteDao, apiService)

        // ViewModel initialisieren
        viewModel = ViewModelProvider(this, CollectionsViewModelFactory(repository))[CollectionsViewModel::class.java]

        // Setze das Layout des RecyclerViews und initialisiere den Adapter
        binding.recyclerViewCollections.layoutManager = LinearLayoutManager(requireContext())
        adapter = CollectionsAdapter { quote, comment ->
            viewModel.addCommentToQuote(quote, comment)
        }
        binding.recyclerViewCollections.adapter = adapter

        // Beobachte die gespeicherten Zitate und aktualisiere den Adapter
        viewModel.savedQuotes.observe(viewLifecycleOwner) { quotes ->
            adapter.updateData(quotes ?: emptyList())
        }

        // Beobachte Erfolgsmeldungen vom ViewModel und zeige eine Snackbar an
        viewModel.commentAddedSuccessfully.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(view, "Kommentar erfolgreich hinzugefügt", Snackbar.LENGTH_SHORT).show()
                viewModel.resetCommentAddedSuccessfully()
            }
        }

        // Kommentar speichern
        binding.saveCommentButton.setOnClickListener {
            val selectedQuote = adapter.getSelectedQuote()
            val comment = binding.commentEditText.text.toString()

            if (selectedQuote != null) {
                viewModel.addCommentToQuote(selectedQuote, comment)
                binding.commentEditText.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Bitte wählen Sie ein Zitat aus.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}