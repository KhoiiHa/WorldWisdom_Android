package com.example.projektworldwisdom.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentCategoryQuotesBinding
import com.example.projektworldwisdom.databinding.ItemQuoteBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel

/**
 * CategoryQuotesFragment
 *
 * Decision A:
 * CategoryOverview -> (tap category) -> CategoryQuotes -> QuoteDetails
 *
 * NOTE: This Fragment uses `fragment_category_quotes.xml` via ViewBinding.
 * The adapter is kept local and lightweight (no extra ViewModel) to keep MVVM clean.
 */
class CategoryQuotesFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val args: CategoryQuotesFragmentArgs by navArgs()

    private var _binding: FragmentCategoryQuotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var quotesAdapter: CategoryQuotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        quotesAdapter = CategoryQuotesAdapter(
            onQuoteClick = { quote ->
                // QuoteDetails uses SafeArgs (QuoteDetailsFragmentArgs). Pass the required values.
                sharedViewModel.selectQuote(quote)

                val action = CategoryQuotesFragmentDirections
                    .actionCategoryQuotesFragmentToQuoteDetailsFragment(
                        quoteId = quote.favoriteKey,
                        quoteText = quote.quote,
                        author = quote.author,
                        category = quote.category,
                        sourceUrl = quote.source.takeIf { it.isNotBlank() },
                        tags = quote.tags.toTypedArray(),
                        isFavorite = quote.isFavorite
                    )
                findNavController().navigate(action)
            },
            onFavoriteToggle = { quote ->
                // Single source of truth: persist favorite + publish updated list via SharedViewModel.
                sharedViewModel.toggleFavorite(quote)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Args from nav_graph.xml (Category mode OR Author mode)
        val category = args.category?.trim().orEmpty()
        val authorSlug = args.authorSlug?.trim().orEmpty()
        val authorName = args.authorName?.trim().orEmpty()

        val isAuthorMode = authorSlug.isNotBlank()

        // UI
        val screenTitle = when {
            isAuthorMode && authorName.isNotBlank() ->
                getString(R.string.category_quotes_title_author, authorName)

            isAuthorMode ->
                getString(R.string.category_quotes_title_fallback)

            category.isNotBlank() ->
                getString(R.string.category_quotes_title_category, category)

            else ->
                getString(R.string.category_quotes_title_fallback)
        }

        binding.toolbar.title = screenTitle
        binding.headerTitle.text = screenTitle
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // Empty-state copy (mode-specific)
        if (isAuthorMode) {
            binding.emptyStateTitle.setText(R.string.category_quotes_empty_title_author)
            binding.emptyStateSubtitle.setText(R.string.category_quotes_empty_subtitle_author)
        } else {
            binding.emptyStateTitle.setText(R.string.category_quotes_empty_title_category)
            binding.emptyStateSubtitle.setText(R.string.category_quotes_empty_subtitle_category)
        }

        // Empty-state CTA
        binding.btnEmptyAction.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = quotesAdapter

        // Single source of truth: observe the central quotes list and derive a list from it.
        sharedViewModel.quotes.observe(viewLifecycleOwner) { allQuotes ->
            val safeAll = allQuotes.orEmpty()

            val filtered = if (isAuthorMode) {
                // Author mode: filter by stable slug (preferred), fallback to name if needed.
                safeAll.filter {
                    it.authorSlug.trim().equals(authorSlug, ignoreCase = true) ||
                        (authorName.isNotBlank() && it.author.trim().equals(authorName, ignoreCase = true))
                }
            } else {
                // Category mode (derived from the observed list)
                if (category.isNotBlank()) {
                    safeAll.filter { it.category.trim().equals(category, ignoreCase = true) }
                } else {
                    emptyList()
                }
            }

            quotesAdapter.submitList(filtered)
            binding.headerCount.text = resources.getQuantityString(
                R.plurals.category_quotes_results_count,
                filtered.size,
                filtered.size
            )

            val isEmpty = filtered.isEmpty()
            binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        // Optional: keep origin for debugging; set to null for a cleaner UI
        binding.toolbar.subtitle = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ----------------------------
// Adapter (small, local, portfolio-friendly)
// ----------------------------

private class CategoryQuotesAdapter(
    private val onQuoteClick: (Quote) -> Unit,
    private val onFavoriteToggle: (Quote) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Quote, CategoryQuotesAdapter.QuoteViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuoteViewHolder(
            binding = binding,
            getItemAt = { pos -> currentList.getOrNull(pos) },
            onQuoteClick = onQuoteClick,
            onFavoriteToggle = onFavoriteToggle
        )
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class QuoteViewHolder(
        private val binding: ItemQuoteBinding,
        private val getItemAt: (Int) -> Quote?,
        private val onQuoteClick: (Quote) -> Unit,
        private val onFavoriteToggle: (Quote) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                getItemAt(pos)?.let(onQuoteClick)
            }

            binding.btnFavorite.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                getItemAt(pos)?.let(onFavoriteToggle)
            }
        }

        fun bind(item: Quote) {
            binding.quoteText.text = item.quote

            val author = item.author.trim()
            binding.quoteAuthor.text = if (author.isNotBlank()) {
                binding.root.context.getString(R.string.quote_details_author_prefix, author)
            } else {
                ""
            }

            val iconRes = if (item.isFavorite) {
                R.drawable.ic_star_filled
            } else {
                R.drawable.ic_star_outline
            }
            binding.btnFavorite.setImageResource(iconRes)
        }
    }

    companion object {
        private val DIFF = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Quote>() {
            override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem.favoriteKey == newItem.favoriteKey
            }

            override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem == newItem
            }
        }
    }
}