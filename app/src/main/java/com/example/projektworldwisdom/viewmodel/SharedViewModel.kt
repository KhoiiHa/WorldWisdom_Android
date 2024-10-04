package com.example.projektworldwisdom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

class SharedViewModel (application: Application) : AndroidViewModel(application) {
    // LiveData für den ausgewählten Autor
    private val _selectedAuthor = MutableLiveData<Int>()
    val selectedAuthor: LiveData<Int> get() = _selectedAuthor



    // LiveData für das ausgewählte Zitat
    private val _selectedQuote = MutableLiveData<Quote>()
    val selectedQuote: LiveData<Quote> get() = _selectedQuote

    // Methode zum Setzen des ausgewählten Autors
    fun selectAuthor(authorId: Int) {
        _selectedAuthor.value = authorId
    }

    // Methode zum Setzen des ausgewählten Zitats
    fun selectQuote(quote: Quote) {
        _selectedQuote.value = quote
    }
}