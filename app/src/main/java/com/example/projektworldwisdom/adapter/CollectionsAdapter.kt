package com.example.projektworldwisdom.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.model.Quote
import com.google.android.material.snackbar.Snackbar


class CollectionsAdapter(
    private var savedQuotes: List<Quote>,
    private val onDeleteClick: (Quote) -> Unit,
    private val onCommentSave: (Quote, String) -> Unit, // Callback für das Speichern des Kommentars
    private val onCommentDelete: (Quote) -> Unit // Callback für das Löschen des Kommentars
) : RecyclerView.Adapter<CollectionsAdapter.CollectionsViewHolder>() {

    inner class CollectionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quoteTextView: TextView = itemView.findViewById(R.id.quoteTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        private val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        private val keywordsTextView: TextView = itemView.findViewById(R.id.keywordsTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val commentButton: Button = itemView.findViewById(R.id.commentButton)
        private val commentSection: LinearLayout = itemView.findViewById(R.id.comment_section)
        private val commentInput: EditText = itemView.findViewById(R.id.comment_input)
        private val saveCommentButton: Button = itemView.findViewById(R.id.save_comment_button)
        private val deleteCommentButton: Button = itemView.findViewById(R.id.delete_comment_button) // Button zum Löschen des Kommentars

        fun bind(quote: Quote) {
            // Setze das Zitat, den Autor und die Tags
            quoteTextView.text = quote.content
            val authorText = SpannableString("Autor: ${quote.author.name}")
            authorText.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            authorTextView.text = authorText

            tagTextView.text = quote.author.tag
            val keywordsText = SpannableString("Keywords: ${quote.keywords}")
            keywordsText.setSpan(StyleSpan(Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            keywordsTextView.text = keywordsText

            // Kommentarbereich: Zeige den vorhandenen Kommentar, falls er schon existiert
            if (quote.comments?.isNotEmpty() == true) {
                commentInput.setText(quote.comments)
                commentSection.visibility = View.VISIBLE
            } else {
                commentSection.visibility = View.GONE
            }

            // Delete-Button Click-Listener
            deleteButton.setOnClickListener {
                quote.isSaved = !quote.isSaved
                onDeleteClick(quote)
            }

            // Comment-Button Click-Listener (zum Anzeigen oder Verstecken des Kommentarbereichs)
            commentButton.setOnClickListener {
                commentSection.visibility = if (commentSection.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            // Speichern-Button Click-Listener
            saveCommentButton.setOnClickListener {
                val comment = commentInput.text.toString() // Hole den Kommentar aus dem EditText
                if (comment.isNotEmpty()) {
                    onCommentSave(quote, comment) // Übergib das Zitat und den Kommentar an das ViewModel
                    commentSection.visibility = View.GONE // Bereich nach dem Speichern schließen
                    Snackbar.make(itemView, "Kommentar gespeichert", Snackbar.LENGTH_SHORT).show()
                }
            }

            // Löschen-Button Click-Listener
            deleteCommentButton.setOnClickListener {
                onCommentDelete(quote) // Rufe die Funktion zum Löschen des Kommentars auf
                commentInput.text.clear() // Leere das Kommentar-Input-Feld
                commentSection.visibility = View.GONE // Schließe den Kommentarbereich
                Snackbar.make(itemView, "Kommentar gelöscht", Snackbar.LENGTH_SHORT).show() // Zeige Snackbar an
            }
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