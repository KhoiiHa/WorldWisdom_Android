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
 * NOTE: This Fragment currently builds its UI programmatically so the navigation flow works immediately,
 * even before we add the XML layout. In the next step we can swap this to fragment_category_quotes.xml
 * without changing the logic.
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
                        quoteId = quote.id,
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
        val origin = args.origin?.trim().orEmpty().ifBlank { "overview" }
        val authorSlug = args.authorSlug?.trim().orEmpty()
        val authorName = args.authorName?.trim().orEmpty()

        val isAuthorMode = authorSlug.isNotBlank()

        // UI
        val screenTitle = if (isAuthorMode) {
            if (authorName.isNotBlank()) "Zitate von $authorName" else "Zitate vom Autor"
        } else {
            category.ifBlank { "Quotes" }
        }

        binding.toolbar.title = screenTitle
        binding.headerTitle.text = screenTitle
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = quotesAdapter

        // Single source of truth: observe the central quotes list and derive a list from it.
        sharedViewModel.quotes.observe(viewLifecycleOwner) { allQuotes ->
            val safeAll = allQuotes.orEmpty()

            val filtered = if (isAuthorMode) {
                // Author mode: filter by stable slug (preferred), fallback to name if needed.
                safeAll.filter { it.authorSlug == authorSlug || (authorName.isNotBlank() && it.author == authorName) }
            } else {
                // Category mode
                if (category.isNotBlank()) {
                    sharedViewModel.getQuotesByCategory(category)
                } else {
                    emptyList()
                }
            }

            quotesAdapter.submit(filtered)
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
// UI models + adapter (small, local, no overengineering)
// ----------------------------

private class CategoryQuotesAdapter(
    private val onQuoteClick: (Quote) -> Unit,
    private val onFavoriteToggle: (Quote) -> Unit
) : RecyclerView.Adapter<CategoryQuotesAdapter.QuoteViewHolder>() {

    private val items = mutableListOf<Quote>()

    fun submit(newItems: List<Quote>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuoteViewHolder(
            binding = binding,
            onQuoteClick = onQuoteClick,
            onFavoriteToggle = onFavoriteToggle
        )
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class QuoteViewHolder(
        private val binding: ItemQuoteBinding,
        private val onQuoteClick: (Quote) -> Unit,
        private val onFavoriteToggle: (Quote) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var bound: Quote? = null

        init {
            binding.root.setOnClickListener { bound?.let(onQuoteClick) }
            binding.btnFavorite.setOnClickListener { bound?.let(onFavoriteToggle) }
        }

        fun bind(item: Quote) {
            bound = item

            binding.quoteText.text = item.quote

            val author = item.author.trim()
            binding.quoteAuthor.text = if (author.isNotBlank()) "– $author" else ""

            val iconRes = if (item.isFavorite) {
                R.drawable.ic_star_filled
            } else {
                R.drawable.ic_star_outline
            }
            binding.btnFavorite.setImageResource(iconRes)
        }
    }
}