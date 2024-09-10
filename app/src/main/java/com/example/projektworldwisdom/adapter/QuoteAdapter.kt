package com.example.projektworldwisdom.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemQuoteBinding
import com.example.projektworldwisdom.model.Quote

class QuoteAdapter(private var quotes: List<Quote>) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(val binding: ItemQuoteBinding) : RecyclerView.ViewHolder(binding.root)

    // Interface für den Klick-Listener
    interface OnItemClickListener {
        fun onItemClick(quote: Quote)
    }

    // Variable zum Speichern des Klick-Listeners
    private var listener: OnItemClickListener? = null

    // Methode zum Setzen des Klick-Listeners
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val currentQuote = quotes[position]
        holder.binding.quoteText.text = currentQuote.content ?: "Kein Zitat verfügbar"
        holder.binding.quoteAuthor.text = "- ${currentQuote.author ?: "Unbekannt"}"

        // Klick-Listener hinzufügen
        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentQuote)
        }
    }

    override fun getItemCount(): Int {
        return quotes.size
    }

    fun updateQuotes(newQuotes: List<Quote>) {
        this.quotes = newQuotes
        notifyDataSetChanged()
    }
}