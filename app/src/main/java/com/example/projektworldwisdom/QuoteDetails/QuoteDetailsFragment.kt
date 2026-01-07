package com.example.projektworldwisdom.quote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
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
import android.content.res.ColorStateList
import com.google.android.material.color.MaterialColors

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

            // Keep Source button in sync if the quote data changes
            updateSourceUi(updated.source)
        }

        // Favorite toggle
        binding.btnFavorite.setOnClickListener {
            val quote = findQuoteOrNull() ?: buildQuoteFromArgs()
            binding.btnFavorite.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            // UI sofort aktualisieren (fühlt sich “live” an)
            currentIsFavorite = !currentIsFavorite
            updateFavoriteUi(currentIsFavorite)

            // State im ViewModel togglen (Single Source of Truth)
            viewModel.toggleFavorite(quote)
        }

        // Source öffnen
        binding.btnSource.setOnClickListener {
            binding.btnSource.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val latestSource = findQuoteOrNull()?.source ?: args.sourceUrl.orEmpty()
            openUrl(latestSource)
        }

        // Autor Details öffnen
        binding.btnAuthorDetails.setOnClickListener {
            binding.btnAuthorDetails.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            // Wenn kein Autor vorhanden ist, macht AuthorDetails keinen Sinn.
            // (Button ist in renderFromArgs() ohnehin verborgen.)
            if (args.author.isNullOrBlank()) return@setOnClickListener

            val quote = findQuoteOrNull() ?: buildQuoteFromArgs()
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

        binding.btnAuthorDetails.isVisible = author.isNotBlank()

        val category = args.category?.trim().orEmpty()
        binding.quoteCategory.isVisible = category.isNotBlank()
        binding.quoteCategory.text = category

        updateFavoriteUi(currentIsFavorite)
        renderTags(args.tags?.toList().orEmpty())

        // Source Button nur zeigen wenn URL vorhanden
        updateSourceUi(args.sourceUrl)
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

        // Tint: favorited -> primary, otherwise -> onSurfaceVariant (ruhiger, Material-konform)
        val tintAttr = if (isFavorite) {
            com.google.android.material.R.attr.colorPrimary
        } else {
            com.google.android.material.R.attr.colorOnSurfaceVariant
        }
        val tintColor = MaterialColors.getColor(binding.btnFavorite, tintAttr)
        binding.btnFavorite.imageTintList = ColorStateList.valueOf(tintColor)

        // Label + accessibility
        val labelText = if (isFavorite) {
            getString(R.string.quote_details_favorite_saved)
        } else {
            getString(R.string.quote_details_favorite_label)
        }
        binding.favoriteLabel.text = labelText
        binding.btnFavorite.contentDescription = labelText
    }

    private fun updateSourceUi(sourceUrl: String?) {
        val hasSource = !sourceUrl.isNullOrBlank()
        binding.btnSource.isVisible = hasSource
        binding.btnSource.isEnabled = hasSource
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

    private fun buildQuoteFromArgs(): Quote {
        return Quote(
            id = args.quoteId,
            author = args.author?.trim().orEmpty(),
            quote = args.quoteText?.trim().orEmpty(),
            category = args.category?.trim().orEmpty(),
            tags = args.tags?.toList().orEmpty(),
            isFavorite = currentIsFavorite,
            description = "",
            source = args.sourceUrl?.trim().orEmpty(),
            authorImageURLs = emptyList()
        )
    }

    private fun findQuoteOrNull(): Quote? {
        return viewModel.quotes.value?.firstOrNull { it.id == args.quoteId }
    }

    private fun navigateToAuthorDetails(quote: Quote) {
        val action = QuoteDetailsFragmentDirections.actionQuoteDetailsFragmentToAuthorDetailsFragment(
            authorSlug = quote.authorSlug,
            authorName = quote.author,
            authorDescription = null,
            authorBio = quote.description.takeIf { it.isNotBlank() },
            authorSourceUrl = quote.source.takeIf { it.isNotBlank() }
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