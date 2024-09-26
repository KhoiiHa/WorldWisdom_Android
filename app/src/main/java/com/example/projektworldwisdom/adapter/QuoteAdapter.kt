package com.example.projektworldwisdom.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemQuoteBinding
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

class QuoteAdapter(
    private var quotes: List<Quote> = emptyList(),
    private var authors: List<Author> = emptyList()
) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(val binding: ItemQuoteBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onItemClick(quote: Quote)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val currentQuote = quotes[position]

        // Verwende die Map, um den passenden Autor schnell zu finden
        val currentAuthor = currentQuote.authorName?.let { authorName ->
            authors.associateBy { it.name }[authorName]
        }

        // Zitat anzeigen
        holder.binding.quoteTextView.text = currentQuote.content?.takeIf { it.isNotBlank() }
            ?: "Zitat nicht verfügbar"

        // Autor und Tag anzeigen
        holder.binding.quoteAuthor.text = currentAuthor?.name?.let { "- $it" } ?: "- Unbekannter Autor"
        holder.binding.tagTextView.text = currentAuthor?.tag ?: "Beruf nicht verfügbar"

        // Klick-Ereignis
        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentQuote)
        }
    }

    override fun getItemCount(): Int = quotes.size

    // Funktion, um die Daten zu aktualisieren
    fun updateData(newQuotes: List<Quote>, newAuthors: List<Author>) {
        if (this.quotes != newQuotes || this.authors != newAuthors) {
            this.quotes = newQuotes
            this.authors = newAuthors
            notifyDataSetChanged() // Überlege Verwendung von DiffUtil für bessere Performance
        }
    }
}