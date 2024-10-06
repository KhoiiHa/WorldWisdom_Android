package com.example.projektworldwisdom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel


class QuotesAdapter(
    private var quotes: List<Quote>,
    private val sharedViewModel: SharedViewModel,
    private val onQuoteClick: (Quote) -> Unit,
    private val onSaveClick: (Quote) -> Unit
) : RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder>() {

    private var allQuotes: List<Quote> = quotes // Speichere die ursprüngliche Liste der Zitate

    inner class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quoteTextView: TextView = itemView.findViewById(R.id.quoteTextView)
        private val quoteAuthor: TextView = itemView.findViewById(R.id.quoteAuthor)
        private val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        private val saveQuoteButton: ImageButton = itemView.findViewById(R.id.saveQuoteButton)

        fun bind(quote: Quote) {
            quoteTextView.text = quote.content
            quoteAuthor.text = quote.author.name // Zeigt den Namen des Autors an
            tagTextView.text = quote.author.tag // Zeigt den Tag des Zitats an

            // Setze die Klick-Listener
            itemView.setOnClickListener {
                sharedViewModel.selectQuote(quote) // Speichere das ausgewählte Zitat im SharedViewModel
                onQuoteClick(quote) // Rufe die Callback-Methode auf
            }
            saveQuoteButton.setOnClickListener {
                onSaveClick(quote)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quotes[position])
    }

    override fun getItemCount() = quotes.size

    // Methode zum Aktualisieren der Zitate
    fun updateQuotes(newQuotes: List<Quote>) {
        allQuotes = newQuotes // Aktualisiere die ursprüngliche Liste
        quotes = newQuotes // Setze die aktuelle Liste der Zitate auf die neue Liste
        notifyDataSetChanged() // Benachrichtigt den Adapter, dass sich die Daten geändert haben
    }


}