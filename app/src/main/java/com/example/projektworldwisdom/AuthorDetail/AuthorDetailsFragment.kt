package com.example.projektworldwisdom.authordetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding

class AuthorDetailsFragment : Fragment() {

    private val viewModel: AuthorDetailsViewModel by viewModels()
    private lateinit var binding: FragmentAuthorDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte das authorQuote LiveData und aktualisiere die UI
                viewModel.authorQuote.observe(viewLifecycleOwner) { quote ->
                    quote?.let {
                        binding.authorQuote.text = it.content
                    }
                }

        // Klick-Listener für den Button zum Laden eines neuen Zitats
        binding.loadNewQuoteButton.setOnClickListener {
            authorSlug?.let { viewModel.loadRandomQuoteByAuthor(it) }
        }

        // Beobachtet Fehler-Updates
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehler nach Anzeige zurücksetzen
            }
        }

        // Holen Sie die Autor-ID oder den Namen aus den Argumenten
        val authorId = arguments?.getString("authorId")

        // Wenn die Autor-ID vorhanden ist, laden Sie die Autor-Details
        authorId?.let { viewModel.loadAuthorDetails(it) }

        // Beobachten Sie die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            author?.let {
                // Autor-Details in den Views anzeigen
                binding.authorName.text = it.name
                binding.authorBio.text = it.link
                binding.authorQuoteCount.text = "Anzahl der Zitate: ${it.quoteCount}"
            }
        }

        // Beobachtet den Ladezustand
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Hier können Sie eine Ladeanzeige oder einen Fortschrittsbalken einfügen, wenn isLoading true ist
            // Beispiel: binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}