package com.example.projektworldwisdom.allquotes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projektworldwisdom.repository.QuoteRepository

class AllQuotesViewModelFactory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllQuotesViewModel::class.java)) {
            Log.d("AllQuotesViewModelFactory", "Creating AllQuotesViewModel")
            return AllQuotesViewModel(repository) as T
        }
        Log.e("AllQuotesViewModelFactory", "Unknown ViewModel class: ${modelClass.name}")
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}