package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.local.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import kotlinx.coroutines.launch
import androidx.lifecycle.liveData

class QuoteViewModelRepo(private val repository: QuoteRepository) : ViewModel() {

    val quotes = liveData {
        emit(repository.getAllQuotes())
    }

    fun getQuoteById(id: String) = liveData<Quote?> {
        emit(repository.getQuoteById(id))
    }

    fun saveQuote(quote: Quote) = viewModelScope.launch {
        repository.insertQuote(quote)
    }

    fun deleteQuote(quote: Quote) = viewModelScope.launch {
        repository.deleteQuote(quote)
    }
}