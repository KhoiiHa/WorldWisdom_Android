package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projektworldwisdom.model.Quote

/**
 * CollectionViewModel (XML)
 *
 * WICHTIG: Die Collection zeigt in diesem Projekt die lokal gespeicherten Favoriten.
 * Das Laden/Syncen der Quotes (API) und das Favoriten-Management laufen zentral über SharedViewModel.
 *
 * Dieses ViewModel ist bewusst schlank und hält nur den UI-State für die Collection.
 * (Kein Firebase, kein Netzwerk, kein Random-Quote-Fetch.)
 */
class CollectionViewModel : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>>(emptyList())
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    /**
     * Wird vom Fragment/SharedViewModel gefüttert, sobald sich die Favoriten ändern.
     */
    fun setFavorites(favorites: List<Quote>) {
        _quotes.value = favorites
        _error.value = null
        _isLoading.value = false
    }

    /**
     * Optional: wenn du im Fragment kurz einen Loading-State zeigen willst.
     */
    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
        if (isLoading) {
            _error.value = null
        }
    }

    /**
     * Optional: Fehlerstate setzen (z.B. falls SharedViewModel einen Fehler meldet).
     */
    fun setError(message: String?) {
        _error.value = message
        _isLoading.value = false
    }
}