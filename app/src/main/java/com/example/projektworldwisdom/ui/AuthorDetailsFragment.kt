package com.example.projektworldwisdom.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.viewmodel.AuthorDetailsViewModel
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class AuthorDetailsFragment : Fragment() {
    private lateinit var binding: FragmentAuthorDetailsBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: AuthorDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorDetailsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte den ausgewählten Autor
        sharedViewModel.selectedAuthor.observe(viewLifecycleOwner) { authorId ->
           viewModel.pickedAuthor(authorId)
            Log.d("AuthorDetailsFragment", "Selected Author ID: $authorId")
        }

        viewModel.pickedAuthor.observe(viewLifecycleOwner){ author ->
            Log.d("AuthorDetailsFragment", "Selected Author: $author")
            author?.let {
            // Aktualisiere die UI mit den Autorinformationen
            binding.authorName.text = it.name
            binding.authorBiography.text = it.biography
            binding.authorTag.text = it.tag
            binding.authorLink.text = it.link

            // Zitat des Autors laden
            viewModel.loadQuoteForAuthor(it.id)
        }

        }
//
        // Beobachte das Zitat des Autors
        viewModel.authorQuote.observe(viewLifecycleOwner) { quote ->
            quote?.let {
                binding.authorQuote.text = it.content
            }
        }

        // Button-Click-Listener für "Neues Zitat laden"
        binding.loadNewQuoteButton.setOnClickListener {
            sharedViewModel.selectedAuthor.value?.let { author ->
                viewModel.loadNewQuote(author) // Neues Zitat laden
            }
        }

        // Link klickbar machen
        binding.authorLink.setOnClickListener {
            openAuthorLink(binding.authorLink.text.toString())
        }
    }

    private fun openAuthorLink(link: String) {
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // Prüfe, ob eine App zum Öffnen des Links vorhanden ist
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}