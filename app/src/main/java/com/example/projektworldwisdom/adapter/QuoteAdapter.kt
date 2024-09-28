package com.example.projektworldwisdom.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemQuoteBinding
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

class QuoteAdapter(
    private var quotes: List<Quote> = emptyList(),
    private var authors: List<Author> = emptyList(),
    private var onSaveClick: (Quote) -> Unit // Callback für den Speichern-Button
) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(val binding: ItemQuoteBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onItemClick(quote: Quote)
    }

    private var listener: OnItemClickListener? = null
    private var selectedQuote: Quote? = null // Aktuell ausgewähltes Zitat

    // Setze den Click-Listener für Zitate
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    // Neu: Methode zum Setzen des Save-Click-Listeners
    fun setOnSaveClickListener(listener: (Quote) -> Unit) {
        onSaveClick = listener // Callback für den Speichern-Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val currentQuote = quotes[position]

        // Finde den passenden Autor für das aktuelle Zitat
        val currentAuthor = currentQuote.authorName?.let { authorName ->
            authors.find { it.name == authorName }
        }

        // Zitat anzeigen
        holder.binding.quoteTextView.text = currentQuote.content?.takeIf { it.isNotBlank() }
            ?: "Zitat nicht verfügbar"

        // Autor und Tag anzeigen
        holder.binding.quoteAuthor.text = currentAuthor?.name?.let { "- $it" } ?: "- Unbekannter Autor"
        holder.binding.tagTextView.text = currentAuthor?.tag ?: "Beruf nicht verfügbar"

        // Klick-Ereignis für das Zitat selbst
        holder.itemView.setOnClickListener {
            selectQuote(currentQuote) // Setze das ausgewählte Zitat
            listener?.onItemClick(currentQuote) // Benachrichtige den Listener über den Klick
        }

        // Klick-Ereignis für den Speichern-Button
        holder.binding.saveQuoteButton.setOnClickListener {
            onSaveClick(currentQuote) // Benachrichtige über den Speichern-Button-Klick
        }

        // Sichtbarkeit oder Zustand basierend auf der Auswahl setzen
        holder.itemView.isSelected = (currentQuote == selectedQuote)
    }

    override fun getItemCount(): Int = quotes.size

    // Funktion, um die Daten zu aktualisieren
    fun updateData(newQuotes: List<Quote>, newAuthors: List<Author>) {
        // Prüfe, ob sich die Zitate oder Autoren geändert haben
        if (this.quotes != newQuotes || this.authors != newAuthors) {
            this.quotes = newQuotes
            this.authors = newAuthors
            notifyDataSetChanged() // Benachrichtige RecyclerView über die Änderungen
        }
    }

    // Methode zum Auswählen eines Zitats
    fun selectQuote(quote: Quote) {
        val previousSelectedQuote = selectedQuote
        selectedQuote = quote

        // Aktualisiere nur das vorherige und das neue ausgewählte Zitat
        val previousIndex = quotes.indexOf(previousSelectedQuote)
        val newIndex = quotes.indexOf(quote)

        // Überprüfe, ob die Indizes gültig sind
        if (previousIndex >= 0) notifyItemChanged(previousIndex) // Vorherige Auswahl aktualisieren
        if (newIndex >= 0) notifyItemChanged(newIndex) // Neue Auswahl aktualisieren
    }

//    // Methode zum Abrufen des aktuell ausgewählten Zitats
//    fun getSelectedQuote(): Quote? {
//        return selectedQuote
//    }
}