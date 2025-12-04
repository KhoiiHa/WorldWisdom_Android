package com.example.projektworldwisdom.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding
import com.example.projektworldwisdom.viewmodel.SharedViewModel

class AuthorDetailsFragment : Fragment() {

    private var _binding: FragmentAuthorDetailsBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel, wird von HomeFragment & AuthorDetailsFragment gemeinsam genutzt
    private val sharedViewModel: SharedViewModel by activityViewModels()

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

        // Beobachte das aktuell ausgewählte Author-Objekt aus dem SharedViewModel
        sharedViewModel.selectedAuthor.observe(viewLifecycleOwner) { author ->
            if (author == null) {
                // Falls nichts gesetzt ist, einfach nichts anzeigen (oder später Placeholder-UI)
                return@observe
            }

            binding.authorName.text = author.name
            binding.authorBio.text = author.bio

            val link = author.link.orEmpty()
            if (link.isNotBlank()) {
                binding.authorLink.text = link
                binding.authorLink.visibility = View.VISIBLE
            } else {
                binding.authorLink.text = ""
                binding.authorLink.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}