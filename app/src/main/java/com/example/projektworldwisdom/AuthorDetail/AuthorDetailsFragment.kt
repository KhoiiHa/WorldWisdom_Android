package com.example.projektworldwisdom.AuthorDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding

class AuthorDetailsFragment : Fragment() {

    private val viewModel: AuthorDetailsViewModel by lazy {
        ViewModelProvider(this)[AuthorDetailsViewModel::class.java]
    }
    private lateinit var binding: FragmentAuthorDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorDetailsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
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

        val args = arguments?.let { AuthorDetailsFragmentArgs.fromBundle(it) }
        val authorSlug = args?.authorSlug

        // Lade die Autoren-Details
        authorSlug?.let { viewModel.loadAuthorDetails(it) }

        // Beobachtet die authorDetails LiveData und aktualisiere es
        viewModel.authorDetails.observe(viewLifecycleOwner) { author ->
            author?.let {
                binding.authorName.text = it.name
                binding.authorBio.text = it.bio
                binding.authorLink.text = it.link
                binding.authorLink.visibility = if (it.link?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
        }
    }
}