package com.example.projektworldwisdom.AuthorDetail

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
        // Fehlerbehandlung
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()

                viewModel._error.value = null
            }
        }

        // die ID oder den Namen des Autors aus den Argumenten
        val authorId = arguments?.getString("authorId") // Oder authorName

        // Lade die Autoren-Details
        authorId?.let { viewModel.loadAuthorDetails(it) }

        // Beobachtet die authorDetails LiveData und aktualisiere es
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            author?.let {
                binding.authorName.text = it.name
                binding.authorBio.text = it.bio
                binding.authorQuoteCount.text = "Anzahl der Zitate: ${it.quoteCount}"
            }
        }

    }
}