package com.example.projektworldwisdom.authordetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import androidx.navigation.fragment.navArgs
import com.example.projektworldwisdom.allquotes.AllQuotesViewModel
import com.example.projektworldwisdom.mockApi.MockApi
import com.google.android.material.snackbar.Snackbar

class AuthorDetailsFragment : Fragment() {


    private lateinit var binding: FragmentAuthorDetailsBinding

    private val allQuotesViewmodel: AllQuotesViewModel by activityViewModels()
    private val viewModel: AuthorDetailsViewModel by viewModels { createFactory() }

    // Factory-Methode für das ViewModel
    private fun createFactory(): AuthorDetailsViewModelFactory {
        val quoteDao = QuoteDatabase.getDatabase(requireContext()).quoteDao()
        return AuthorDetailsViewModelFactory(QuoteRepository(quoteDao))
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

        // Hole die Argumente aus den Safe Args
        val args: AuthorDetailsFragmentArgs by navArgs()
        val authorName = args.authorName // Autorname aus den Argumenten
        val quote = args.quote // Zitat aus den Argumenten

        // Debugging-Logs
        Log.d("AuthorDetailsFragment", "Author Name: $authorName")
        Log.d("AuthorDetailsFragment", "Quote: ${quote.content}")

        allQuotesViewmodel.quotesDetails.observe(viewLifecycleOwner) { quotes ->
            Log.d("AuthorDetailsFragment", "Quotes Details: $quotes")
            // Setze die Autor-Details in der UI
            binding.authorName.text = quotes.authorName
//            binding.authorTag.text = quotes.content
            binding.authorQuote.text = quotes.content
        }

        viewModel.setInitialQuote(quote) // Setze das initiale Zitat im ViewModel

        // Lade die Autor-Details
        val authorSlug = authorName.lowercase().replace(" ", "-") // Erstelle den Slug für den Autor
        viewModel.loadAuthorDetails(authorSlug) // Lade die Autor-Details mit dem Slug

        // Beobachte die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            // Hole die Argumente aus den Safe Args
            val args: AuthorDetailsFragmentArgs by navArgs()
            val authorName = args.authorName // Autorname aus den Argumenten
            val quote = args.quote // Zitat aus den Argumenten

            Log.d("AuthorDetailsFragment", "Author Details Updated: $author") // Debugging-Log
            author?.let { authorDetails ->

                // Link nur anzeigen, wenn vorhanden und nicht leer
                binding.authorLink.apply {
                    isVisible = !authorDetails.link.isNullOrEmpty() // Sichtbarkeit des Links setzen
                    text = authorDetails.link ?: "" // Setze den Text des Links
                    setOnClickListener {
                        authorDetails.link?.let { link -> // Wenn ein Link vorhanden ist
                            val intent =
                                Intent(Intent.ACTION_VIEW, Uri.parse(link)) // Erstelle einen Intent
                            startActivity(intent) // Starte die Aktivität mit dem Link
                        }
                    }
                }

                // Lade das Bild des Autors (falls vorhanden)
                binding.authorImage.load(authorDetails.imageUrl) {
                    crossfade(true) // Überblende die Bildanzeige
                    placeholder(R.drawable.ic_launcher_background) // Setze Platzhalterbild
                    error(R.drawable.ic_launcher_background) // Setze Bild bei Fehler
                }
            } ?: run {
                // Fehlerfall behandeln
                Log.e(
                    "AuthorDetailsFragment",
                    "Fehler beim Laden der Autor-Details"
                ) // Fehlerprotokoll
                Toast.makeText(
                    requireContext(),
                    "Autor-Details konnten nicht geladen werden",
                    Toast.LENGTH_SHORT
                ).show() // Fehlernachricht
            }
        }

        // Beobachte Fehler-Updates
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .show() // Zeige Snackbar für Fehler
                viewModel.clearError() // Fehlernachricht zurücksetzen
            }
        }

        // Klick-Listener für den Button zum Laden eines neuen Zitats
        binding.loadNewQuoteButton.setOnClickListener {
            viewModel.loadNewQuote() // Lade ein neues Zitat
        }

        // Zurück-Button verknüpfen
        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // Navigiere zur vorherigen Ansicht
        }
    }
}