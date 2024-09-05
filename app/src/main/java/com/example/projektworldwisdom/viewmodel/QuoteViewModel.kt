package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.FirebaseRepository


class QuoteViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes


    fun saveQuoteInCollection(quoteId: Int) {
        firebaseRepository.saveQuote(quoteId)
    }


}