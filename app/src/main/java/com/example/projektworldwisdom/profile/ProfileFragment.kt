package com.example.projektworldwisdom.profile

import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.projektworldwisdom.R
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGithub = view.findViewById<MaterialButton>(R.id.btnProfileGithub)
        val btnCaseStudy = view.findViewById<MaterialButton>(R.id.btnProfileCaseStudy)
        val btnAppInfo = view.findViewById<MaterialButton>(R.id.btnProfileAppInfo)
        val btnSettings = view.findViewById<MaterialButton>(R.id.btnProfileSettings)

        btnGithub.setOnClickListener {
            openUrl(getString(R.string.profile_github_url))
        }

        btnCaseStudy.setOnClickListener {
            openUrl(getString(R.string.profile_case_study_url))
        }

        btnSettings.setOnClickListener {
            // Navigate to Settings
            try {
                val navController = findNavController()
                val destinationId = R.id.settingsFragment

                // Prevent duplicate navigation taps
                if (navController.currentDestination?.id == destinationId) return@setOnClickListener

                navController.navigate(destinationId)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_navigation),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnAppInfo.setOnClickListener {
            // App Info (MVP placeholder)
            // Using destination-id navigation avoids crashes when an action-id is missing
            try {
                val navController = findNavController()
                val destinationId = R.id.settingsFragment

                // Prevent duplicate navigation taps
                if (navController.currentDestination?.id == destinationId) return@setOnClickListener

                navController.navigate(destinationId)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_navigation),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openUrl(url: String) {
        val cleaned = url.trim()

        if (cleaned.isBlank()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.profile_url_missing),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Be forgiving: allow storing links without scheme in strings.xml
        val normalized = if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            cleaned
        } else {
            "https://$cleaned"
        }

        val uri = normalized.toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_no_browser),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}