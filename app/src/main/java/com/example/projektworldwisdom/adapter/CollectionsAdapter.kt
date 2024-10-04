package com.example.projektworldwisdom.adapter

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
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val commentButton: Button = itemView.findViewById(R.id.commentButton)

        fun bind(quote: Quote) {
            quoteTextView.text = quote.content
            authorTextView.text = quote.author.name // Verwende den Namen des Autors
            tagTextView.text = quote.keywords // Sicherstellen, dass das Tag-Feld in der Quote-Klasse existiert

            // Delete button click listener
            deleteButton.setOnClickListener { onDeleteClick(quote) }

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