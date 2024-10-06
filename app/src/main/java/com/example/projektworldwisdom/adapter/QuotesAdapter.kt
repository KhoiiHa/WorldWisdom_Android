package com.example.projektworldwisdom.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
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
        private val keywordsTextView: TextView = itemView.findViewById(R.id.keywordsTextView)
        private val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        private val saveQuoteButton: ImageButton = itemView.findViewById(R.id.saveQuoteButton)

        fun bind(quote: Quote) {
            // Setze den Text für das Zitat
            quoteTextView.text = quote.content

            // Für den Autor
            val authorText = SpannableString("Autor: ${quote.author.name}")
            authorText.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // "Autor:" fett
            quoteAuthor.text = authorText // Zeigt den Autor mit dem fettgedruckten "Autor:" an

            // Zeigt den Tag des Zitats an
            tagTextView.text = quote.author.tag

            // Für die Keywords
            val keywordsText = SpannableString("Keywords: ${quote.keywords}")
            keywordsText.setSpan(StyleSpan(Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // "Keywords:" fett
            keywordsTextView.text = keywordsText // Zeigt die Keywords mit dem fettgedruckten "Keywords:" an

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