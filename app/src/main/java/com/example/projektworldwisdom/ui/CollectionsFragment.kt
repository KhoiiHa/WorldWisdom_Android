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

class CollectionsFragment : Fragment() {
    private lateinit var binding: FragmentCollectionsBinding
    private val viewModel: CollectionsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte die gespeicherten Zitate
        viewModel.savedQuotes.observe(viewLifecycleOwner) { savedQuotes ->
           if (savedQuotes!= null) { // Adapter initialisieren und an die RecyclerView binden
               binding.savedQuotesRecyclerView.adapter = CollectionsAdapter(savedQuotes,
                   onDeleteClick = { quote ->
                       // Zitat löschen
                       viewModel.deleteQuote(quote)
                   },
                   onCommentClick = { quote ->
                       // Kommentar zu dem Zitat hinzufügen oder bearbeiten
                       addComment(quote)
                   })
           }

        }

        // Gespeicherte Zitate abrufen
        viewModel.getSavedQuotes()
    }

    private fun addComment(quote: Quote) {
        // Logik zum Hinzufügen eines Kommentars, z.B. ein Dialog öffnen
//         Zum Beispiel: CommentDialogFragment.show(quote)
    }
}