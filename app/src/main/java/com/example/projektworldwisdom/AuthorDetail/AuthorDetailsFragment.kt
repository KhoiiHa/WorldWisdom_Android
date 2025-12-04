package com.example.projektworldwisdom.AuthorDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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

        // authorSlug aus den Safe Args lesen
        val slug = args.authorSlug

        // TODO: Später AuthorDetailsViewModel + Repository nutzen, um
        // anhand des Slugs die vollständigen Autor:innen-Daten aus API/Firestore zu laden.
        // Für den aktuellen Stand zeigen wir den Slug bzw. verwenden ihn als Platzhalter.

        binding.apply {
            // Vorerst den Slug anzeigen – später durch author.name ersetzen
            authorName.text = slug

            // Platzhalter / leere Werte für weitere Felder, bis die API-Logik steht
            authorBio.text = ""
            authorLink.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}