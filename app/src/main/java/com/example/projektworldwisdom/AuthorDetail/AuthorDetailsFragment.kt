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

    // ViewModel zur Verwaltung der Autor-Details
    private val viewModel: AuthorDetailsViewModel by lazy {
        val quoteDao = QuoteDatabase.getDatabase(requireContext()).quoteDao() // Hole die Datenbank
        val repository = QuoteRepository(quoteDao) // Erstelle das Repository
        AuthorDetailsViewModel(repository) // Erstelle das ViewModel
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

        // Beobachte die Zitatdetails
        allQuotesViewmodel.quotesDetails.observe(viewLifecycleOwner) { quotes ->
            Log.d("AuthorDetailsFragment", "Quotes Details: $quotes")

            // Setze die Autor-Details in der UI
            binding.authorName.text = quotes.authorName
            binding.authorQuote.text = quotes.content

            // Hole den Autor anhand des Autorennamens
            val authorNameFromQuote = quotes.authorName ?: ""
            viewModel.getAuthorByName(authorNameFromQuote) // Aufruf der Methode

            // Beobachte die Autor-Details
            viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
                Log.d("AuthorDetailsFragment", "Author Details Updated: $author")
                if (author != null) {
                    binding.authorTag.text = author.tag // Setze das Tag-TextView
                    binding.authorTag.isVisible = true // Mache das Tag sichtbar
                } else {

                    Toast.makeText(requireContext(), "Autor nicht gefunden", Toast.LENGTH_SHORT).show()
                    binding.authorTag.isVisible = false // Verstecke das Tag, wenn nicht vorhanden
                }
            }
        }

        viewModel.setInitialQuote(quote) // Setze das initiale Zitat im ViewModel

        // Lade die Autor-Details
        val authorSlug = authorName.lowercase().replace(" ", "-") // Erstelle den Slug für den Autor
        viewModel.loadAuthorDetails(authorSlug) // Lade die Autor-Details mit dem Slug

        // Beobachte die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            // Debugging-Log
            Log.d("AuthorDetailsFragment", "Author Details Updated: $author")

            // Hole die Argumente aus den Safe Args
            val args: AuthorDetailsFragmentArgs by navArgs()
            val quote = args.quote // Zitat aus den Argumenten

            author?.let { authorDetails ->
                // Link nur anzeigen, wenn vorhanden und nicht leer
                binding.authorLink.apply {
                    isVisible = !authorDetails.link.isNullOrEmpty() // Sichtbarkeit des Links setzen
                    text = authorDetails.link ?: "" // Setze den Text des Links
                    setOnClickListener {
                        authorDetails.link?.let { link -> // Wenn ein Link vorhanden ist
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)) // Erstelle einen Intent
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

                // Hier das Zitat setzen
                binding.authorQuote.text = quote.content // Setze das Zitat hier
            } ?: run {
                // Fehlerfall behandeln
                Log.e("AuthorDetailsFragment", "Fehler beim Laden der Autor-Details") // Fehlerprotokoll
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