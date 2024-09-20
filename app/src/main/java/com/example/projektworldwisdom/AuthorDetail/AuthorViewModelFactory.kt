package com.example.projektworldwisdom.authordetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projektworldwisdom.repository.QuoteRepository

class AuthorDetailsViewModelFactory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthorDetailsViewModel::class.java) -> {
                AuthorDetailsViewModel(repository) as T
            }
            else -> {
                Log.e("ViewModelFactory", "Unknown ViewModel class: ${modelClass.name}")
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}