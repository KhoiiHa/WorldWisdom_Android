package com.example.projektworldwisdom.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
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

    // Methode zum Setzen des Save-Click-Listeners
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
        val oldQuotes = quotes
        quotes = newQuotes
        authors = newAuthors

        val diffCallback = QuoteDiffCallback(oldQuotes, newQuotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        diffResult.dispatchUpdatesTo(this) // Benachrichtige die RecyclerView über die Änderungen
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
}

// DiffUtil.Callback für die Zitate
class QuoteDiffCallback(
    private val oldList: List<Quote>,
    private val newList: List<Quote>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id // Anpassen, um ID oder eindeutige Eigenschaft zu vergleichen
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition] // Vergleich der Inhalte
    }
}