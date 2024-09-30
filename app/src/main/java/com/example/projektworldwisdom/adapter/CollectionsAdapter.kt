package com.example.projektworldwisdom.adapter


import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemCollectionQuoteBinding
import com.example.projektworldwisdom.model.Quote

class CollectionsAdapter(
    private val onCommentClick: (Quote, String) -> Unit,
    private val onDeleteClick: (Quote) -> Unit
) : RecyclerView.Adapter<CollectionsAdapter.CollectionViewHolder>() {

    private var quotes: MutableList<Quote> = mutableListOf() // MutableList verwenden
    private var selectedQuote: Quote? = null

    fun updateData(newQuotes: List<Quote>) {
        Log.d("CollectionsAdapter", "Updating data with ${newQuotes.size} quotes.")

        // Leere die aktuelle Liste und füge die neuen Zitate hinzu
        quotes.clear()
        quotes.addAll(newQuotes)
        notifyDataSetChanged() // Adapter benachrichtigen, dass sich die Daten geändert haben
    }

    fun getSelectedQuote(): Quote? = selectedQuote

    inner class CollectionViewHolder(val binding: ItemCollectionQuoteBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(quote: Quote) {
            // Anzeige des Zitats und Autors
            binding.quoteTextView.text = "\"${quote.content ?: "Kein Zitat verfügbar"}\""
            binding.authorTextView.text = "- ${quote.authorName ?: "Unbekannt"}"

            // OnClickListener für den Kommentar-Button
            binding.commentButton.setOnClickListener {
                if (selectedQuote != quote) {
                    selectedQuote = quote // Setze das ausgewählte Zitat
                    onCommentClick(quote, "")
                } else {
                    selectedQuote = null // Setze die Auswahl zurück, falls erneut geklickt
                }
            }

            // OnClickListener für den Löschen-Button
            binding.deleteButton.setOnClickListener {
                onDeleteClick(quote) // Aufruf der Lösch-Funktion
            }

            // Kommentare anzeigen
            setupComments(quote)
        }

        private fun setupComments(quote: Quote) {
            // Hier zeigen wir die Kommentare direkt in einem TextView an
            binding.commentsTextView.text = quote.comments.joinToString("\n") { comment -> "- $comment" }
            binding.commentsTextView.visibility = if (quote.comments.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ItemCollectionQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(quotes[position])
    }

    override fun getItemCount(): Int = quotes.size
}