package com.example.projektworldwisdom.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.projektworldwisdom.adapter.CollectionsAdapter
import com.example.projektworldwisdom.databinding.FragmentCollectionsBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.CollectionsViewModel
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class CollectionsFragment : Fragment() {
    private lateinit var binding: FragmentCollectionsBinding
    private val sharedViewModel: SharedViewModel by activityViewModels() // SharedViewModel verwenden
    private lateinit var savedQuotesAdapter: CollectionsAdapter // Umbenennung für Klarheit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisiere den Adapter und binde ihn an die RecyclerView
        savedQuotesAdapter = CollectionsAdapter(emptyList(), // Beginne mit einer leeren Liste
            onDeleteClick = { quote ->
                // Zitat löschen
                sharedViewModel.deleteQuote(quote)
            },
            onCommentClick = { quote ->
                // Kommentar zu dem Zitat hinzufügen oder bearbeiten
                addComment(quote)
            }
        )

        binding.savedQuotesRecyclerView.adapter = savedQuotesAdapter

        // Beobachte die gespeicherten Zitate
        sharedViewModel.savedQuotes.observe(viewLifecycleOwner) { savedQuotes ->
            // Update den Adapter mit den neuen Zitatdaten
            savedQuotesAdapter.updateQuotes(savedQuotes)
        }

        // Gespeicherte Zitate abrufen
        sharedViewModel.getSavedQuotes()
    }

    private fun addComment(quote: Quote) {
        // Logik zum Hinzufügen eines Kommentars, z.B. ein Dialog öffnen
        // Zum Beispiel: CommentDialogFragment.show(quote)
        // Hier könnte ein Dialog oder eine Eingabemethode zur Bearbeitung des Kommentars implementiert werden.
    }
}