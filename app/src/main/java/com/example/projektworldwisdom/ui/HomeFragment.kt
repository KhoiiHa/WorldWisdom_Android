package com.example.projektworldwisdom.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.adapter.QuotesAdapter
import com.example.projektworldwisdom.databinding.FragmentHomeBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.HomeViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte die Zitate
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            // Adapter initialisieren und an die RecyclerView binden
            binding.quotesList.adapter = QuotesAdapter(quotes, { quote ->
                // Hier gehst du zum nächsten Screen mit den Details für die Quote
                navigateToAuthorDetails(quote)
            }, { quote ->
                // Hier speicherst du die Quote in den CollectionsScreen
                saveQuote(quote)
            })
        }

        // Zitate abrufen
        viewModel.getQuotes()
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        // Logik für die Navigation zu den Details des Autors
        // Überprüfen, ob der Autor vorhanden ist, um zu vermeiden, dass null übergeben wird
        quote.author?.let {
            // Beispiel mit NavController:
//             val action = HomeFragmentDirections.actionHomeFragmentToAuthorDetailFragment(it.id)
//             findNavController().navigate(action)
        }
    }

    private fun saveQuote(quote: Quote) {
        viewModel.saveQuote(quote)
    }
}
