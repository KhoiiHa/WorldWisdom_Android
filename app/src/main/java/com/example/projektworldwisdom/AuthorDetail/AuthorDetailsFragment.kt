package com.example.projektworldwisdom.AuthorDetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.databinding.FragmentAuthorDetailsBinding

class AuthorDetailsFragment : Fragment() {

    private var _binding: FragmentAuthorDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: AuthorDetailsFragmentArgs by navArgs()

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

        // Toolbar: Back navigation
        binding.toolbarAuthorDetails.apply {
            // Use a safe built-in back icon (no extra drawable needed)
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        // authorSlug aus den Safe Args lesen
        val slug = args.authorSlug

        // Optional: wenn wir beim Navigieren echte Detaildaten mitgeben,
        // nutzen wir diese direkt (kein extra Fetch nötig).
        val authorName = args.authorName?.takeIf { it.isNotBlank() } ?: slug

        // Toolbar title matches the current author
        binding.toolbarAuthorDetails.title = authorName

        val authorDescription = args.authorDescription?.takeIf { it.isNotBlank() }
        val authorBio = args.authorBio?.takeIf { it.isNotBlank() }
        val authorSourceUrl = args.authorSourceUrl?.takeIf { it.isNotBlank() }

        bindContent(
            authorName = authorName,
            authorDescription = authorDescription,
            authorBio = authorBio,
            authorSourceUrl = authorSourceUrl
        )
    }

    private fun bindContent(
        authorName: String,
        authorDescription: String?,
        authorBio: String?,
        authorSourceUrl: String?
    ) {
        binding.apply {
            this.authorName.text = authorName

            // Kurzbeschreibung unter dem Namen (Fallback keeps the screen "finished")
            this.authorDescription.text = authorDescription ?: "Autorprofil"

            // Bio / long description (Fallback is a friendly empty-state)
            this.authorBio.text = authorBio ?: (
                "Noch keine Biografie verfügbar.\n\n" +
                    "In der nächsten Ausbaustufe laden wir Beschreibung & Quelle aus der Mock-API."
                )

            // Source link (only visible if we have a real URL)
            if (authorSourceUrl.isNullOrBlank()) {
                authorLinkCard.visibility = View.GONE
                authorLink.visibility = View.GONE
                authorLink.setOnClickListener(null)
            } else {
                authorLinkCard.visibility = View.VISIBLE
                authorLink.visibility = View.VISIBLE
                authorLink.text = authorSourceUrl
                authorLink.setOnClickListener {
                    runCatching {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authorSourceUrl)))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}