package com.example.projektworldwisdom.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
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
        tvVersion.text = BuildConfig.VERSION_NAME.ifBlank { getString(R.string.common_placeholder_dash) }

        // Reuse your existing URLs from Profile (keine Dopplung)
        val githubUrl = getString(R.string.profile_github_url).trim()
        val caseStudyUrl = getString(R.string.profile_case_study_url).trim()

        btnGithub.isEnabled = githubUrl.isNotBlank()
        btnGithub.alpha = if (btnGithub.isEnabled) 1f else 0.5f
        btnGithub.setOnClickListener { openUrl(githubUrl) }

        btnCaseStudy.isEnabled = caseStudyUrl.isNotBlank()
        btnCaseStudy.alpha = if (btnCaseStudy.isEnabled) 1f else 0.5f
        btnCaseStudy.setOnClickListener { openUrl(caseStudyUrl) }
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    private fun openUrl(url: String) {
        val cleaned = url.trim()

        if (cleaned.isBlank()) {
            showToast(R.string.profile_url_missing)
            return
        }

        val normalized = when {
            cleaned.startsWith("http://", ignoreCase = true) -> cleaned
            cleaned.startsWith("https://", ignoreCase = true) -> cleaned
            else -> "https://$cleaned"
        }

        val uri = normalized.toUri()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_no_browser)
        }
    }
}