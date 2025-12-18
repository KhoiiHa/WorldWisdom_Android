package com.example.projektworldwisdom.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.BuildConfig
import com.example.projektworldwisdom.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.settingsToolbar)
        val tvAppName = view.findViewById<TextView>(R.id.tvSettingsAppNameValue)
        val tvVersion = view.findViewById<TextView>(R.id.tvSettingsVersionValue)

        val btnGithub = view.findViewById<MaterialButton>(R.id.btnSettingsGithub)
        val btnCaseStudy = view.findViewById<MaterialButton>(R.id.btnSettingsCaseStudy)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        tvAppName.text = getString(R.string.app_name)
        tvVersion.text = BuildConfig.VERSION_NAME

        // Reuse your existing URLs from Profile (keine Dopplung)
        btnGithub.setOnClickListener {
            openUrl(getString(R.string.profile_github_url))
        }

        btnCaseStudy.setOnClickListener {
            openUrl(getString(R.string.profile_case_study_url))
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

        val normalized = if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            cleaned
        } else {
            "https://$cleaned"
        }

        val intent = Intent(Intent.ACTION_VIEW, normalized.toUri())

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