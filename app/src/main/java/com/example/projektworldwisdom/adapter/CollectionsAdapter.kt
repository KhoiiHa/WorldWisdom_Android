package com.example.projektworldwisdom.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.model.Quote


class CollectionsAdapter(
    private var savedQuotes: List<Quote>,
    private val onDeleteClick: (Quote) -> Unit,
    private val onCommentClick: (Quote) -> Unit
) : RecyclerView.Adapter<CollectionsAdapter.CollectionsViewHolder>() {

    inner class CollectionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quoteTextView: TextView = itemView.findViewById(R.id.quoteTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        private val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        private val keywordsTextView: TextView = itemView.findViewById(R.id.keywordsTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val commentButton: Button = itemView.findViewById(R.id.commentButton)

        fun bind(quote: Quote) {
            quoteTextView.text = quote.content
            // Für den Autor
            val authorText = SpannableString("Autor: ${quote.author.name}")
            authorText.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // Fett für "Autor:"
            authorTextView.text = authorText

            tagTextView.text = quote.author.tag // Verwende den Tag des Autors
            // Für die Keywords
            val keywordsText = SpannableString("Keywords: ${quote.keywords}")
            keywordsText.setSpan(StyleSpan(Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // Fett für "Keywords:"
            keywordsTextView.text = keywordsText

            // Delete button click listener
            deleteButton.setOnClickListener {
                quote.isSaved = !quote.isSaved
                onDeleteClick(quote) }

            // Comment button click listener
            commentButton.setOnClickListener { onCommentClick(quote) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection_quote, parent, false)
        return CollectionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionsViewHolder, position: Int) {
        holder.bind(savedQuotes[position])
    }

    override fun getItemCount() = savedQuotes.size

    // Methode zum Aktualisieren der Liste
    fun updateQuotes(newQuotes: List<Quote>) {
        savedQuotes = newQuotes
        notifyDataSetChanged()
    }
}