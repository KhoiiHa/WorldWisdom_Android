package com.example.projektworldwisdom.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemCollectionQuoteBinding
import com.example.projektworldwisdom.model.Quote

class CollectionsAdapter(
    private val onCommentClick: (Quote, String) -> Unit
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
            // Anzeige des Zitats, Autors und Tags mit Fehlerbehandlung
            binding.quoteTextView.text = "\"${quote.content ?: "Kein Zitat verfügbar"}\""
            binding.authorTextView.text = "- ${quote.authorName ?: "Unbekannt"}"
//            binding.tagTextView.text = quote.tag ?: "Kein Tag verfügbar"

            // OnClickListener für den Kommentar-Button
            binding.commentButton.setOnClickListener {
                selectedQuote = quote
                onCommentClick(quote, "")
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