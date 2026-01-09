package com.example.projektworldwisdom.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.ItemQuoteBinding
import com.example.projektworldwisdom.model.Quote
import com.google.android.material.color.MaterialColors

class QuoteAdapter(
    private val onQuoteClick: ((Quote) -> Unit)? = null,
    /**
     * NEW (empfohlen): liefert den Ziel-Zustand (true = speichern, false = entfernen)
     * → damit Home/Collection wirklich als Toggle funktionieren.
     */
    private val onFavoriteToggle: ((quote: Quote, isFavorite: Boolean) -> Unit)? = null,
    /**
     * Legacy: lässt bestehenden Code weiterlaufen.
     * Wenn `onFavoriteToggle` gesetzt ist, wird diese Callback nicht mehr genutzt.
     */
    private val onFavoriteClick: ((Quote) -> Unit)? = null
) : ListAdapter<Quote, QuoteAdapter.QuoteViewHolder>(DIFF_CALLBACK) {

    // Keeps the UI responsive while the ViewModel updates the source-of-truth list.
    // Key = quote.favoriteKey (stable identity), Value = desired favorite state.
    private val favoriteOverrides = mutableMapOf<String, Boolean>()

    class QuoteViewHolder(val binding: ItemQuoteBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Legacy-API (damit bestehender Code nicht kaputt geht).
     * Kann später entfernt werden, wenn überall Lambdas genutzt werden.
     */
    interface OnItemClickListener {
        fun onItemClick(quote: Quote)
    }

    private var legacyListener: OnItemClickListener? = null

    @Suppress("unused")
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.legacyListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = getItem(position)

        holder.binding.quoteText.text = quote.quote
        holder.binding.quoteAuthor.text = holder.itemView.context.getString(
            R.string.quote_author_format,
            quote.author
        )

        // ⭐ Favoriten-Status anzeigen (Drawables + klare Tint-Logik)
        val favoriteButton = holder.binding.btnFavorite
        val quoteKey = quote.favoriteKey
        val displayFavorite = favoriteOverrides[quoteKey] ?: quote.isFavorite
        bindFavoriteState(favoriteButton, displayFavorite)

        // ⭐ Toggle (optimistic UI + Source of Truth = ViewModel/submitList)
        favoriteButton.setOnClickListener {
            // Use the most compatible position API (works even if newer APIs are not available)
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            val current = getItem(pos)
            val currentKey = current.favoriteKey
            val currentDisplayedState = favoriteOverrides[currentKey] ?: current.isFavorite
            val newState = !currentDisplayedState

            // Optimistic UI: update immediately.
            favoriteOverrides[currentKey] = newState
            bindFavoriteState(favoriteButton, newState)

            // Pass an updated model to callbacks (so screens can reflect the latest state immediately)
            val updated = current.copy(isFavorite = newState)

            // NEW: Preferred toggle callback (lets the VM decide add/remove)
            if (onFavoriteToggle != null) {
                onFavoriteToggle.invoke(updated, newState)
            } else {
                // Legacy behavior (existing screens might still call "toggleFavorite(quote)")
                onFavoriteClick?.invoke(updated)
            }
        }

        // Item-Tap → Details
        holder.itemView.setOnClickListener {
            // Always use the latest adapter position to avoid stale data
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            val current = getItem(pos)
            val currentKey = current.favoriteKey
            // Include optimistic favorite override (so Details receives the displayed state)
            val displayedFavorite = favoriteOverrides[currentKey] ?: current.isFavorite
            val currentForClick = if (displayedFavorite == current.isFavorite) {
                current
            } else {
                current.copy(isFavorite = displayedFavorite)
            }

            // Priorität: neue Lambda-API → fallback: alte Interface-API
            onQuoteClick?.invoke(currentForClick) ?: legacyListener?.onItemClick(currentForClick)
        }
    }

    /**
     * Kompatibel mit deinem bisherigen Code (Home/Collection rufen updateQuotes()).
     * Intern nutzen wir ListAdapter + DiffUtil für saubere Updates.
     */
    private fun bindFavoriteState(button: AppCompatImageButton, isFavorite: Boolean) {
        val starRes = if (isFavorite) {
            R.drawable.ic_star_filled
        } else {
            R.drawable.ic_star_outline
        }
        button.setImageResource(starRes)

        // Accessibility: announce the correct action depending on state
        val contentDescRes = if (isFavorite) {
            R.string.content_desc_favorite_remove
        } else {
            R.string.content_desc_favorite_add
        }
        button.contentDescription = button.context.getString(contentDescRes)

        val tintColor = if (isFavorite) {
            // Highlight color for saved quotes
            ContextCompat.getColor(button.context, R.color.favorite_star_on)
        } else {
            // Neutral, theme-safe color for the outline state
            MaterialColors.getColor(button, com.google.android.material.R.attr.colorOnSurfaceVariant)
        }

        button.imageTintList = ColorStateList.valueOf(tintColor)
        button.alpha = 1f
    }

    fun updateQuotes(newQuotes: List<Quote>) {
        // Compatibility helper for existing screens (Home/Collection).
        // `submitList()` already clears optimistic overrides once the source-of-truth list confirms them.
        submitList(newQuotes.toList())
    }

    override fun submitList(list: List<Quote>?) {
        if (list == null) {
            favoriteOverrides.clear()
            super.submitList(null)
            return
        }

        // Remove overrides that have been confirmed by the source-of-truth list.
        for (q in list) {
            val key = q.favoriteKey
            val override = favoriteOverrides[key]
            if (override != null && override == q.isFavorite) {
                favoriteOverrides.remove(key)
            }
        }

        // Also remove overrides for items that are no longer present.
        val keys = list.asSequence().map { it.favoriteKey }.toSet()
        val toRemove = favoriteOverrides.keys.filter { it !in keys }
        toRemove.forEach { favoriteOverrides.remove(it) }

        super.submitList(list)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Quote>() {
            override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                // Falls du später eine stabile ID im Model hast (z.B. quoteId), kannst du hier darauf wechseln.
                return oldItem.favoriteKey == newItem.favoriteKey
            }

            override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem == newItem
            }
        }
    }
}