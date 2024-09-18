//package com.example.projektworldwisdom.allquotes
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.projektworldwisdom.repository.QuoteRepository

//class AllQuotesViewModelFactory(private val repository: QuoteRepository) : ViewModelProvider.Factory {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(AllQuotesViewModel::class.java)) {
//            return AllQuotesViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}