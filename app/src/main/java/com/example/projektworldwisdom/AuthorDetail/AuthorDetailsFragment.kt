package com.example.projektworldwisdom.authordetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import androidx.navigation.fragment.navArgs
import com.example.projektworldwisdom.mockApi.MockApi

class AuthorDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAuthorDetailsBinding
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

        // Holt die Argumente aus den Safe Args
        val args: AuthorDetailsFragmentArgs by navArgs()
        val authorName = args.authorName
        val quote = args.quote

        // Log zur Überprüfung des authorName
        Log.d("AuthorDetailsFragment", "Lade Autor-Details für: $authorName")

        // Setze das initiale Zitat im ViewModel, wenn das Zitat vorhanden ist
        quote?.let {
            viewModel.setInitialQuote(it)
        }

        // Lade Autor-Details (Umwandlung in authorSlug)
        val authorSlug = convertNameToSlug(authorName)

        // Log zur Überprüfung des authorSlug
        Log.d("AuthorDetailsFragment", "Generated slug for author: $authorSlug")

        viewModel.loadAuthorDetails(authorSlug)

        // Beobachte die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            if (author != null) {
                binding.authorName.text = author.name

                // Überprüfe, ob der Link nicht leer ist, bevor du ihn anzeigst
                if (!author.link.isNullOrEmpty()) {
                    binding.authorLink.text = author.link
                    binding.authorLink.setOnClickListener {
                        // Öffne den Link im Browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(author.link))
                        startActivity(intent)
                    }
                } else {
                    binding.authorLink.text = "Kein Link verfügbar"
                }

                // Zeige das Bild des Autors an
                binding.authorImage.load(author.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                }

                // Zeige das Tag des Autors an
                binding.authorTag.text = author.tag ?: "Kein Tag verfügbar"

                Log.d("AuthorDetailsFragment", "Autor-Details geladen: $author")

                // Füge hier die Logik zum Abrufen und Anzeigen der Zitate hinzu
                val allQuotes = MockApi.getAllQuotes() // Alle Zitate abrufen
                val authorQuotes =
                    allQuotes.filter { it.authorName == author.name } // Zitate für den Autor filtern

                // Setze das erste Zitat (falls vorhanden)
                if (authorQuotes.isNotEmpty()) {
                    viewModel.setInitialQuote(authorQuotes.first())
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Autor-Details konnten nicht geladen werden",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("AuthorDetailsFragment", "Keine Autor-Details gefunden für: $authorSlug")
            }
        }

        // Beobachte Fehler-Updates
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError() // Fehler zurücksetzen
                Log.e("AuthorDetailsFragment", "Fehler: $it")
            }
        }

        // Beobachte Ladezustände
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Klick-Listener für den Button zum Laden eines neuen Zitats
        binding.loadNewQuoteButton.setOnClickListener {
            viewModel.loadNewQuote()
            Log.d("AuthorDetailsFragment", "Neues Zitat geladen für Autor: $authorName")
        }

        // Beobachte das authorQuote LiveData
        viewModel.authorQuote.observe(viewLifecycleOwner) { newQuote ->
            binding.authorQuote.text = newQuote?.content ?: "Kein Zitat verfügbar"
            Log.d(
                "AuthorDetailsFragment",
                "Aktuelles Zitat: ${newQuote?.content ?: "Kein Zitat verfügbar"}"
            )
        }

        // Beobachte Ladezustand für Zitate
        viewModel.isQuoteLoading.observe(viewLifecycleOwner) { isQuoteLoading ->
            binding.quoteProgressBar.visibility = if (isQuoteLoading) View.VISIBLE else View.GONE
        }
    }

    // Hilfsfunktion zur Umwandlung des Namens in einen Slug
    private fun convertNameToSlug(name: String): String {
        return name.lowercase().replace(" ", "-") // Beispiel für Bindestriche
    }
}