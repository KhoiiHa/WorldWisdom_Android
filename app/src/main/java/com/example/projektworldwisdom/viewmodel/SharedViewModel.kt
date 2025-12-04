package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

class SharedViewModel : ViewModel() {

    // --- Gemeinsamer Auswahl-Status (zwischen mehreren Fragmenten) ---

    // Ausgewählter Autor, z. B. HomeFragment -> AuthorDetailsFragment
    private val _selectedAuthor = MutableLiveData<Author?>()
    val selectedAuthor: LiveData<Author?> = _selectedAuthor

    // Ausgewähltes Zitat, z. B. HomeFragment -> QuoteDetailsFragment (optional für später)
    private val _selectedQuote = MutableLiveData<Quote?>()
    val selectedQuote: LiveData<Quote?> = _selectedQuote

    // --- Setter-Methoden ---

    fun selectAuthor(author: Author) {
        _selectedAuthor.value = author
    }

    fun clearSelectedAuthor() {
        _selectedAuthor.value = null
    }

    fun selectQuote(quote: Quote) {
        _selectedQuote.value = quote
    }

    fun clearSelectedQuote() {
        _selectedQuote.value = null
    }
}