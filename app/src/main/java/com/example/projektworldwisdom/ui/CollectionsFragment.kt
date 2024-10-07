package com.example.projektworldwisdom.ui



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.projektworldwisdom.adapter.CollectionsAdapter
import com.example.projektworldwisdom.databinding.FragmentCollectionsBinding
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class CollectionsFragment : Fragment() {
    private lateinit var binding: FragmentCollectionsBinding
    private val sharedViewModel: SharedViewModel by activityViewModels() // SharedViewModel verwenden
    private lateinit var savedQuotesAdapter: CollectionsAdapter // Umbenennung für Klarheit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        sharedViewModel.loadSavedQuotes() // Lade gespeicherte Zitate aus der Datenbank oder API
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisiere den Adapter und binde ihn an die RecyclerView
        savedQuotesAdapter = CollectionsAdapter(
            emptyList(), // Beginne mit einer leeren Liste
            onDeleteClick = { quote ->
                sharedViewModel.updateQuote(quote) // Zitat löschen oder aktualisieren
            },
            onCommentSave = { quote, comment ->
                sharedViewModel.updateComment(quote, comment) // Speichere den Kommentar im ViewModel
            },
            onCommentDelete = { quote ->
                sharedViewModel.deleteComment(quote) // Lösche den Kommentar im ViewModel
            }
        )

        binding.savedQuotesRecyclerView.adapter = savedQuotesAdapter

        // Beobachte die gespeicherten Zitate
        sharedViewModel.savedQuotes.observe(viewLifecycleOwner) { savedQuotes ->
            if (savedQuotes.isNullOrEmpty()) {
                Log.d("CollectionsFragment", "Anzahl gespeicherter Zitate: ${savedQuotes}")
                // Wenn die Liste leer ist, zeige den emptyStateTextView und verstecke die RecyclerView
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.savedQuotesRecyclerView.visibility = View.GONE
            } else {
                Log.d("CollectionsFragment", "Anzahl gespeicherter Zitate: ${savedQuotes.size}")
                // Wenn es gespeicherte Zitate gibt, zeige die RecyclerView und verstecke den emptyStateTextView
                binding.emptyStateTextView.visibility = View.GONE
                binding.savedQuotesRecyclerView.visibility = View.VISIBLE
                // Update den Adapter mit den neuen Zitatdaten
                savedQuotesAdapter.updateQuotes(savedQuotes)
            }
        }
    }
}