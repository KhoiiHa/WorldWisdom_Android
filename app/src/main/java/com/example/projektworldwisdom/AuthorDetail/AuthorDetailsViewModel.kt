package com.example.projektworldwisdom.AuthorDetail


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch

class AuthorDetailsViewModel : ViewModel() {

    private val _authorDetails = MutableLiveData<Author?>()
    val authorDetails: LiveData<Author?> = _authorDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    val _selectedAuthorSlug = MutableLiveData<String?>()
    val selectedAuthorSlug: LiveData<String?> = _selectedAuthorSlug

    fun loadAuthorDetails(authorSlug: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.searchAuthors(authorSlug)
                _authorDetails.postValue(result.results.firstOrNull())
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Autoren-Details: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Error loading author details", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}