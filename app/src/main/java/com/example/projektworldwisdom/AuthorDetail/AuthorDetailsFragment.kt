package com.example.projektworldwisdom.authordetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository

class AuthorDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAuthorDetailsBinding

    // ViewModel mit der Factory initialisieren (nur für dieses Fragment)
    private val viewModel: AuthorDetailsViewModel by viewModels {
        val apiService = WorldWisdomApi.retrofitService
        val database = QuoteDatabase.getDatabase(requireContext())
        val quoteDao = database.quoteDao()
        val repository = QuoteRepository(quoteDao, apiService)
        AuthorDetailsViewModelFactory(repository)
    }

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
            binding.authorQuote.text = quote?.content ?: "Kein Zitat verfügbar"
        }

        // Klick-Listener für den Button zum Laden eines neuen Zitats
        binding.loadNewQuoteButton.setOnClickListener {
            viewModel.authorDetails.value?.tag?.let { tag ->
                viewModel.loadRandomQuoteByAuthor(tag)
            } ?: run {
                // Fehlerbehandlung, wenn slug null ist
                Toast.makeText(requireContext(), "Autor-Details nicht verfügbar", Toast.LENGTH_SHORT).show()
            }
        }

        // Beobachtet Fehler-Updates
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Holt die Autor-ID oder den Namen aus den Argumenten
        val authorId = arguments?.getString("authorId")

        // Wenn die Autor-ID vorhanden ist, lade Autor-Details
        if (authorId != null) {
            viewModel.loadAuthorDetails(authorId)
            binding.progressBar.visibility = View.VISIBLE // Ladeanzeige anzeigen
        } else {
            // Fehlerbehandlung, wenn authorId null ist
            Toast.makeText(requireContext(), getString(R.string.error_no_author_id), Toast.LENGTH_SHORT).show()
        }

        // Beobachten die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            author?.let {
                // Autor-Details in den Views anzeigen
                binding.authorName.text = it.name
                binding.authorLink.text = it.link
//                binding.authorQuoteCount.text = "Anzahl der Zitate: ${it.quoteCount}"
                binding.authorImage.load(it.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background) // Ersetze durch ein passenderes Platzhalterbild
                    error(R.drawable.ic_launcher_background)     // Ersetze durch ein passenderes Fehlerbild
                }
            }
        }

        // Beobachtet den allgemeinen Ladezustand
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Beobachtet den Ladezustand für Zitate und aktualisiert die Ladeanzeige
        viewModel.isQuoteLoading.observe(viewLifecycleOwner) { isQuoteLoading ->
            binding.quoteProgressBar.visibility = if (isQuoteLoading) View.VISIBLE else View.GONE
        }
    }
}