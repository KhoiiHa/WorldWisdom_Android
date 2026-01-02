package com.example.projektworldwisdom.AuthorDetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.R
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
            setNavigationIcon(R.drawable.ic_arrow_back_24)
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
            val hasSource = !authorSourceUrl.isNullOrBlank()
            authorLinkCard.isVisible = hasSource
            authorLink.isVisible = hasSource

            if (hasSource) {
                authorLink.text = authorSourceUrl

                val click = View.OnClickListener {
                    // Subtle feedback
                    authorLinkCard.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    openUrl(authorSourceUrl!!)
                }

                authorLinkCard.setOnClickListener(click)
                authorLink.setOnClickListener(click)
            } else {
                authorLinkCard.setOnClickListener(null)
                authorLink.setOnClickListener(null)
            }
        }
    }

    private fun openUrl(raw: String) {
        val url = normalizeUrl(raw)
        if (url.isBlank()) {
            Toast.makeText(requireContext(), "Keine Quelle verfügbar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Link konnte nicht geöffnet werden", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""

        val hasScheme = trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)

        return if (hasScheme) trimmed else "https://$trimmed"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}