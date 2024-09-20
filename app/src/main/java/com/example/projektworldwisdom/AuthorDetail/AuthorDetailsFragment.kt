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

    // ViewModel mit der Factory initialisieren
    private val viewModel: AuthorDetailsViewModel by viewModels { createFactory() }

    private fun createFactory(): AuthorDetailsViewModelFactory {
        val apiService = WorldWisdomApi.retrofitService
        val database = QuoteDatabase.getDatabase(requireContext())
        val quoteDao = database.quoteDao()
        val repository = QuoteRepository(quoteDao, apiService)
        return AuthorDetailsViewModelFactory(repository)
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
                viewModel.loadRandomQuoteByAuthor(tag) // Lade ein zufälliges Zitat des Autors
            } ?: run {
                // Fehlerbehandlung, wenn der tag null ist
                Toast.makeText(
                    requireContext(),
                    "Autor-Details nicht verfügbar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Beobachtet Fehler-Updates und zeigt eine Toast-Nachricht an
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehler nach Anzeige zurücksetzen
            }
        }

        // Holt den Autorennamen aus den Argumenten
        val authorName = arguments?.getString("authorName")

        // Wenn der Autorenname vorhanden ist, lade die Autor-Details
        authorName?.let {
            viewModel.loadAuthorDetails(it)
            binding.progressBar.visibility = View.VISIBLE // Ladeanzeige anzeigen
        } ?: run {
            // Fehlerbehandlung, wenn der Autorenname null ist
            Toast.makeText(
                requireContext(),
                getString(R.string.error_no_author_id),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Beobachte die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            author?.let {
                // Autor-Details in den Views anzeigen
                binding.authorName.text = it.name
                binding.authorLink.text = it.link
                // binding.authorQuoteCount.text = "Anzahl der Zitate: ${it.quoteCount}" // Wenn gewünscht, kann die Zitatanzahl angezeigt werden
                binding.authorImage.load(it.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background) // Benutzerdefiniertes Platzhalterbild
                    error(R.drawable.ic_launcher_background)            // Benutzerdefiniertes Fehlerbild
                }
            } ?: run {
                // Fehlerbehandlung, falls keine Autor-Details gefunden werden
                Toast.makeText(
                    requireContext(),
                    "Autor-Details konnten nicht geladen werden",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Beobachte den allgemeinen Ladezustand
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Beobachte den Ladezustand für Zitate
        viewModel.isQuoteLoading.observe(viewLifecycleOwner) { isQuoteLoading ->
            binding.quoteProgressBar.visibility = if (isQuoteLoading) View.VISIBLE else View.GONE
        }
    }
}