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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.local.QuoteDatabase
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import androidx.navigation.fragment.navArgs
import com.example.projektworldwisdom.mockApi.MockApi
import com.google.android.material.snackbar.Snackbar

class AuthorDetailsFragment : Fragment() {

    private var _binding: FragmentAuthorDetailsBinding? = null
    private val binding get() = _binding!!
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
        _binding = FragmentAuthorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hole die Argumente aus den Safe Args
        val args: AuthorDetailsFragmentArgs by navArgs()
        val authorName = args.authorName
        val quote = args.quote

        // Log zur Überprüfung des authorName
        Log.d("AuthorDetailsFragment", "Lade Autor-Details für: $authorName")

        // Setze das initiale Zitat im ViewModel
        viewModel.setInitialQuote(quote)

        // Lade Autor-Details (Umwandlung in authorSlug)
        val authorSlug = authorName.lowercase().replace(" ", "-")

        // Log zur Überprüfung des authorSlug
        Log.d("AuthorDetailsFragment", "Generated slug for author: $authorSlug")

        viewModel.loadAuthorDetails(authorSlug)

        // Beobachte die Autor-Details
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            author?.apply {
                binding.authorName.text = name
                binding.authorTag.text = tag ?: "Kein Tag verfügbar"

                // Link nur anzeigen, wenn vorhanden und nicht leer
                binding.authorLink.isVisible = !link.isNullOrEmpty()
                binding.authorLink.text = link
                binding.authorLink.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }

                // Lade das Bild des Autors mit Coil oder Glide (hier beispielhaft mit Coil)
                binding.authorImage.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                }

                Log.d("AuthorDetailsFragment", "Autor-Details geladen: $author")
            } ?: run {
                // Fehlerfall behandeln, wenn author null ist
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
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
                Log.e("AuthorDetailsFragment", "Fehler: $it")
            }
        }

        // Beobachte Ladezustände für Autordetails
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Aktualisiere die Sichtbarkeit der Ladeanzeige für Autordetails (falls vorhanden)
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
            // Aktualisiere die Sichtbarkeit der Ladeanzeige für Zitate (falls vorhanden)
        }

        // Zurück-Button verknüpfen
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}