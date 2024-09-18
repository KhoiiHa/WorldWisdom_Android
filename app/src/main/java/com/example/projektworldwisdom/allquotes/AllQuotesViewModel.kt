//package com.example.projektworldwisdom.allquotes
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.projektworldwisdom.repository.QuoteRepository
//import com.example.projektworldwisdom.model.Quote
//import com.example.projektworldwisdom.remote.WorldWisdomApi
//import kotlinx.coroutines.launch
//
//class AllQuotesViewModel(private val repository: QuoteRepository) : ViewModel() {
//
//    private val _quotes = MutableLiveData<List<Quote>>()
//    val quotes: LiveData<List<Quote>> = _quotes
//
//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean> = _isLoading
//
//    private val _error = MutableLiveData<String?>()
//    val error: LiveData<String?> = _error

//    fun loadAllQuotes() {
//        viewModelScope.launch {
//            _isLoading.postValue(true)
//            try {
//                val allQuotes = WorldWisdomApi.retrofitService.getAllQuotes()
//                _quotes.postValue(allQuotes)
//                // Speichere die Zitate in der lokalen Datenbank
//                repository.insertQuotes(allQuotes)
//            } catch (e: Exception) {
//                // Fallback: Lade alle Zitate aus der lokalen Datenbank
//                val localQuotes = repository.getAllLocalQuotes()
//                if (!localQuotes.isNullOrEmpty()) {
//                    _quotes.postValue(localQuotes)
//                } else {
//                    _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
//                    Log.e("HomeViewModel", "Fehler beim Laden der Zitate", e)
//                }
//            } finally {
//                _isLoading.postValue(false)
//            }
//        }
//    }
//}