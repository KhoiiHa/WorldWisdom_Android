package com.example.projektworldwisdom.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektworldwisdom.databinding.ItemQuoteBinding
import com.example.projektworldwisdom.model.Quote

class QuoteAdapter(private var quotes: List<Quote>) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(val binding: ItemQuoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val currentQuote = quotes[position]
        holder.binding.quoteText.text = currentQuote.content
        holder.binding.quoteAuthor.text = "- ${currentQuote.author}"
    }

    override fun getItemCount(): Int {
        return quotes.size
    }

    fun updateData(newQuotes: List<Quote>) {
        this.quotes = newQuotes
        notifyDataSetChanged()
    }
}