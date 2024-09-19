//package com.example.projektworldwisdom.remote
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.example.projektworldwisdom.model.Quote
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//
//class QuoteRepositoryFire(
//    private val QuoteApi: WorldWisdomApiService
//) {

//    private val _downloading = MutableLiveData(false)
//    val downloading: LiveData<Boolean> = _downloading
//
//    private val _uploading = MutableLiveData(false)
//    val uploading: LiveData<Boolean> = _uploading
//
//    suspend fun getRandomQuote(id: Int): Quote? {
//        _downloading.postValue(true)
//        return withContext(Dispatchers.IO) {
//            try {
//                val result = QuoteApi.getMultipleRandomQuotes(count = 1)
//                result.results.firstOrNull()
//            } catch (e: Exception) {
//                // Fehlerbehandlung
//                null
//            } finally {
//                _downloading.postValue(false)
//            }
//        }
//    }
//}