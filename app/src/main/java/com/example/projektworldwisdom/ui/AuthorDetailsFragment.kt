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
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class AuthorDetailsFragment : Fragment() {
    private lateinit var binding: FragmentAuthorDetailsBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte den ausgewählten Autor
        sharedViewModel.selectedAuthor.observe(viewLifecycleOwner) { author ->
            author?.let {
                updateUIWithAuthor(it)
                sharedViewModel.loadQuoteForAuthor(it.name) // Zitat laden
            }
        }

        // Beobachte das ausgewählte Zitat
        sharedViewModel.selectedQuote.observe(viewLifecycleOwner) { quote ->
            quote?.let {
                binding.authorQuote.text = it.content // Setze das Zitat
            }
        }

        // Link klickbar machen
        binding.authorLink.setOnClickListener {
            openAuthorLink(binding.authorLink.text.toString())
        }

        // Zurück-Pfeil-Button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateUIWithAuthor(author: Author) {
        binding.authorName.text = author.name
        binding.authorBiography.text = author.biography
        binding.authorTag.text = author.tag
        binding.authorLink.text = author.link
    }

    private fun openAuthorLink(link: String) {
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}