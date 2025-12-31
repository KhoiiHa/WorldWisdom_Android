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
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentCategoryQuotesBinding
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel
import com.google.android.material.card.MaterialCardView

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

        // Args from nav_graph.xml
        val category = args.category
        val origin = args.origin ?: "overview"

        // UI
        binding.toolbar.title = category
        binding.headerTitle.text = category
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = quotesAdapter

        // Single source of truth: observe the central quotes list and derive the category list from it.
        sharedViewModel.quotes.observe(viewLifecycleOwner) {
            val quotesForCategory = sharedViewModel.getQuotesByCategory(category)
            quotesAdapter.submit(quotesForCategory)
            binding.headerCount.text = "${quotesForCategory.size} Ergebnisse"
        }

        // Small debug-friendly subtitle (optional). Remove later if you don’t like it.
        binding.toolbar.subtitle = if (origin.isNotBlank()) "Quelle: $origin" else null
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
        val context = parent.context

        val card = MaterialCardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                val m = dp(context, 12)
                setMargins(m, m, m, 0)
            }
            radius = dp(context, 12).toFloat()
            isClickable = true
            isFocusable = true
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 16))
        }

        val quoteText = TextView(context).apply {
            textSize = 16f
        }

        val metaRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val authorText = TextView(context).apply {
            textSize = 13f
            alpha = 0.75f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val fav = TextView(context).apply {
            textSize = 18f
            setPadding(dp(context, 8))
        }

        metaRow.addView(authorText)
        metaRow.addView(fav)

        container.addView(quoteText)
        container.addView(metaRow)
        card.addView(container)

        return QuoteViewHolder(
            card = card,
            quoteText = quoteText,
            authorText = authorText,
            fav = fav,
            onQuoteClick = onQuoteClick,
            onFavoriteToggle = onFavoriteToggle
        )
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class QuoteViewHolder(
        private val card: MaterialCardView,
        private val quoteText: TextView,
        private val authorText: TextView,
        private val fav: TextView,
        private val onQuoteClick: (Quote) -> Unit,
        private val onFavoriteToggle: (Quote) -> Unit
    ) : RecyclerView.ViewHolder(card) {

        private var bound: Quote? = null

        init {
            card.setOnClickListener {
                bound?.let(onQuoteClick)
            }
            fav.setOnClickListener {
                bound?.let(onFavoriteToggle)
            }
        }

        fun bind(item: Quote) {
            bound = item
            quoteText.text = item.quote
            authorText.text = item.author.takeIf { it.isNotBlank() } ?: ""
            fav.text = if (item.isFavorite) "★" else "☆"
        }
    }

    private fun dp(context: android.content.Context, value: Int): Int {
        val density = context.resources.displayMetrics.density
        return (value * density).toInt()
    }
}