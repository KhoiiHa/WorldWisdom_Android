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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.viewmodel.SharedViewModel


class QuotesAdapter(
    private var quotes: List<Quote>,
    private val sharedViewModel: SharedViewModel,
    private val onQuoteClick: (Quote) -> Unit
) : RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder>() {

    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quoteTextView: TextView = itemView.findViewById(R.id.quoteTextView)
        private val quoteAuthor: TextView = itemView.findViewById(R.id.quoteAuthor)
        private val keywordsTextView: TextView = itemView.findViewById(R.id.keywordsTextView)
        private val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        private val saveQuoteButton: ImageButton = itemView.findViewById(R.id.saveQuoteButton)

        fun bind(quote: Quote, onQuoteClick: (Quote) -> Unit, sharedViewModel: SharedViewModel) {
            // Setze den Text für das Zitat
            quoteTextView.text = quote.content

            // Für den Autor
            quoteAuthor.text = formatText("Autor: ${quote.author.name}", "Autor:")
            // Zeigt den Tag des Zitats an
            tagTextView.text = quote.author.tag

            // Für die Keywords
            keywordsTextView.text = formatText("Keywords: ${quote.keywords}", "Keywords:")

            // Setze den Status des Speichern-Buttons
            updateSaveButton(quote)

            // Setze die Klick-Listener
            itemView.setOnClickListener {
                onQuoteClick(quote)
            }
            saveQuoteButton.setOnClickListener {
                // Toggle-Funktion aufrufen und Callback für Toast-Nachricht bereitstellen
                sharedViewModel.toggleQuoteSavedState(quote) { message ->
                    // Zeige die Toast-Nachricht an
                    Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
                }
                updateSaveButton(quote) // Status des Speichern-Buttons aktualisieren
            }
        }

        private fun updateSaveButton(quote: Quote) {
            // Hier wird entschieden, ob das Zitat gespeichert ist oder nicht
            if (quote.isSaved) {
                saveQuoteButton.setImageResource(R.drawable.bookmark_heart_40px) // Gefüllter Stern
            } else {
                saveQuoteButton.setImageResource(R.drawable.favorite_40px) // Leerer Stern
            }
        }

        private fun formatText(fullText: String, boldText: String): SpannableString {
            val spannableString = SpannableString(fullText)
            val startIndex = fullText.indexOf(boldText)
            val endIndex = startIndex + boldText.length
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quotes[position], onQuoteClick, sharedViewModel)
    }

    override fun getItemCount() = quotes.size

    // Methode zum Aktualisieren der Zitate
    fun updateQuotes(newQuotes: List<Quote>) {
        quotes = newQuotes // Setze die aktuelle Liste der Zitate auf die neue Liste
        notifyDataSetChanged() // Benachrichtigt den Adapter, dass sich die Daten geändert haben
    }
}