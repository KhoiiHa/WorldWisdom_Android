package com.example.projektworldwisdom.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemCollectionQuoteBinding
import com.example.projektworldwisdom.model.Quote

class CollectionsAdapter(
    private val onCommentClick: (Quote, String) -> Unit,
    private val onDeleteClick: (Quote) -> Unit
) : RecyclerView.Adapter<CollectionsAdapter.CollectionViewHolder>() {

    private var quotes: List<Quote> = emptyList()
    private var selectedQuote: Quote? = null

    fun updateData(newQuotes: List<Quote>) {
        quotes = newQuotes
        notifyDataSetChanged()
    }

    fun getSelectedQuote(): Quote? = selectedQuote

    inner class CollectionViewHolder(val binding: ItemCollectionQuoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(quote: Quote) {
            // Anzeige des Zitats und Autors mit Fehlerbehandlung
            binding.quoteTextView.text = "\"${quote.content ?: "Kein Zitat verfügbar"}\""
            binding.authorTextView.text = "- ${quote.authorName ?: "Unbekannt"}"

            // OnClickListener für den Kommentar-Button
            binding.commentButton.setOnClickListener {
                if (selectedQuote != quote) {
                    selectedQuote = quote // Setze das ausgewählte Zitat
                } else {
                    selectedQuote = null // Setze die Auswahl zurück, falls erneut geklickt
                }
                onCommentClick(quote, "")
                Log.d("CollectionsAdapter", "Kommentar Button geklickt für: '${quote.content}' von '${quote.authorName}'")
            }

            // OnClickListener für den Löschen-Button
            binding.deleteButton.setOnClickListener {
                onDeleteClick(quote) // Aufruf der Lösch-Funktion
                Log.d("CollectionsAdapter", "Löschen Button geklickt für: '${quote.content}' von '${quote.authorName}'")
            }

            // Optional: Log-Ausgabe zur Bestätigung der Bindung
            Log.d("CollectionsAdapter", "Binding quote: '${quote.content}' by '${quote.authorName}'")
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