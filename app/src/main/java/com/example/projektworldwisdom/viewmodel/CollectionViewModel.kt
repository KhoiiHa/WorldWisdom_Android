//package com.example.projektworldwisdom.viewmodel

//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.projektworldwisdom.home.HomeViewModel
//import com.example.projektworldwisdom.model.Quote
//import com.example.projektworldwisdom.remote.FirebaseRepository
//import com.example.projektworldwisdom.remote.QuoteRepositoryFire
//import com.example.projektworldwisdom.remote.WorldWisdomApi
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.launch
//import retrofit2.HttpException
//import java.io.IOException

//class CollectionViewModel(private val homeViewModel: HomeViewModel) : ViewModel() {
//
//    private val firebaseRepository = FirebaseRepository()
//    private val quoteRepository = QuoteRepositoryFire(WorldWisdomApi.retrofitService)
//
//    val downloading: LiveData<Boolean> = firebaseRepository.downloading
//
//    // Verwende das quotes LiveData aus dem HomeViewModel
//    val quotes: LiveData<List<Quote>?> = homeViewModel.quotes
//
//    private val _isLoading = MutableLiveData(false)
//    val isLoading: LiveData<Boolean> = _isLoading
//
//    private val _error = MutableLiveData<String?>()
//    val error: LiveData<String?> = _error
//
//
//    init {
//        getQuotes()
//    }


//    fun getQuotes() {
//        viewModelScope.launch {
//            _isLoading.postValue(true)
//            try {
//                val idCollection = firebaseRepository.getUserCollection()
//                val quoteDeferreds = idCollection?.map { id ->
//                    async { quoteRepository.getRandomQuote(id) }
//                }
//                val quotesTMP = quoteDeferreds?.awaitAll()?.filterNotNull() ?: emptyList()
//
//                homeViewModel._quotes.postValue(quotesTMP)
//                _error.postValue(null)
//            } catch (e: IOException) {
//                _error.postValue("Netzwerkfehler: ${e.message}")
//                Log.e("CollectionViewModel", "Network error fetching quotes", e)
//            } catch (e: HttpException) {
//                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
//                Log.e("CollectionViewModel", "HTTP error fetching quotes", e)
//            } finally {
//                _isLoading.postValue(false)
//            }
//        }
//    }

//    fun refreshUserCollection() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val userCollection = firebaseRepository.getUserCollection()
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = "Fehler beim Aktualisieren der Sammlung: ${e.message}"
//                Log.e("CollectionViewModel", "Error refreshing user collection", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }


//}