package com.example.projektworldwisdom.quote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentQuoteDetailsBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel
import com.google.android.material.chip.Chip

class QuoteDetailsFragment : Fragment() {

    private var _binding: FragmentQuoteDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: QuoteDetailsFragmentArgs by navArgs()
    private val viewModel: SharedViewModel by activityViewModels()

    private var currentIsFavorite: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuoteDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initial render aus SafeArgs (damit der Screen sofort da ist)
        currentIsFavorite = args.isFavorite
        renderFromArgs()

        // Live-Sync: wenn Favorit-Status sich irgendwo ändert (Home/Collection), updaten wir hier die UI
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            val updated = quotes.orEmpty().firstOrNull { it.id == args.quoteId } ?: return@observe
            currentIsFavorite = updated.isFavorite
            updateFavoriteUi(currentIsFavorite)
        }

        // Favorite toggle
        binding.btnFavorite.setOnClickListener {
            val quote = findQuoteOrNull()
            if (quote == null) {
                showToast(R.string.quote_details_toast_quote_not_found)
                return@setOnClickListener
            }

            // UI sofort aktualisieren (fühlt sich “live” an)
            currentIsFavorite = !currentIsFavorite
            updateFavoriteUi(currentIsFavorite)

            // State im ViewModel togglen (Single Source of Truth)
            viewModel.toggleFavorite(quote)
        }

        // Source öffnen
        binding.btnSource.setOnClickListener {
            openUrl(args.sourceUrl.orEmpty())
        }

        // Autor Details öffnen
        binding.btnAuthorDetails.setOnClickListener {
            val quote = findQuoteOrNull()
            if (quote == null) {
                // Fallback: wir navigieren trotzdem, aber mit den Daten aus Args
                navigateToAuthorDetailsFallback()
                return@setOnClickListener
            }
            navigateToAuthorDetails(quote)
        }
    }

    private fun renderFromArgs() {
        val authorTitle = args.author?.trim().orEmpty()
        binding.toolbar.title = if (authorTitle.isNotBlank()) authorTitle else getString(R.string.quote_details_title)

        binding.quoteText.text = args.quoteText.orEmpty()

        val author = args.author
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            .orEmpty()

        binding.quoteAuthor.isVisible = author.isNotBlank()
        binding.quoteAuthor.text = if (author.isNotBlank()) {
            getString(R.string.quote_details_author_prefix, author)
        } else {
            ""
        }

        val category = args.category?.trim().orEmpty()
        binding.quoteCategory.isVisible = category.isNotBlank()
        binding.quoteCategory.text = category

        updateFavoriteUi(currentIsFavorite)
        renderTags(args.tags?.toList().orEmpty())

        // Source Button nur zeigen wenn URL vorhanden
        binding.btnSource.isVisible = !args.sourceUrl.isNullOrBlank()
    }

    private fun updateFavoriteUi(isFavorite: Boolean) {
        // Icons: bitte die Namen an deine vorhandenen Star-Drawables anpassen
        // (du nutzt sie bereits im Home-Item)
        val iconRes = if (isFavorite) {
            R.drawable.ic_star_filled
        } else {
            R.drawable.ic_star_outline
        }

        binding.btnFavorite.setImageResource(iconRes)

        // Optional: Text-Hinweis
        binding.favoriteLabel.text = if (isFavorite) {
            getString(R.string.quote_details_favorite_saved)
        } else {
            getString(R.string.quote_details_favorite_label)
        }
    }

    private fun renderTags(tags: List<String>) {
        binding.chipGroupTags.removeAllViews()

        val cleaned = tags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

        if (cleaned.isEmpty()) {
            binding.tagsLabel.visibility = View.GONE
            binding.chipGroupTags.visibility = View.GONE
            return
        }

        binding.tagsLabel.visibility = View.VISIBLE
        binding.chipGroupTags.visibility = View.VISIBLE

        cleaned.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag
                isClickable = false
                isCheckable = false
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    private fun findQuoteOrNull(): Quote? {
        return viewModel.quotes.value?.firstOrNull { it.id == args.quoteId }
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        val action = QuoteDetailsFragmentDirections.actionQuoteDetailsFragmentToAuthorDetailsFragment(
            authorSlug = quote.author,
            authorName = quote.author,
            authorDescription = null,
            authorBio = quote.description.takeIf { it.isNotBlank() },
            authorSourceUrl = quote.source.takeIf { it.isNotBlank() }
        )
        findNavController().navigate(action)
    }

    private fun navigateToAuthorDetailsFallback() {
        val author = args.author.orEmpty()
        val action = QuoteDetailsFragmentDirections.actionQuoteDetailsFragmentToAuthorDetailsFragment(
            authorSlug = author,
            authorName = author,
            authorDescription = null,
            authorBio = null,
            authorSourceUrl = args.sourceUrl?.takeIf { it.isNotBlank() }
        )
        findNavController().navigate(action)
    }

    private fun openUrl(url: String) {
        val normalized = normalizeUrl(url)
        if (normalized.isBlank()) {
            showToast(R.string.quote_details_toast_no_source)
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
            startActivity(intent)
        } catch (e: Exception) {
            showToast(R.string.quote_details_toast_open_url_failed)
        }
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""

        val hasScheme = trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)

        return if (hasScheme) trimmed else "https://$trimmed"
    }

    private fun showToast(@StringRes resId: Int) {
        Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}