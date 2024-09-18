package com.example.projektworldwisdom.authordetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projektworldwisdom.repository.QuoteRepository

class AuthorDetailsViewModelFactory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorDetailsViewModel::class.java)) {
            return AuthorDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}