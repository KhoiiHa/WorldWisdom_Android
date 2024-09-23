package com.example.projektworldwisdom.home

import android.util.Log
import androidx.lifecycle.*
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>?>()
    val quotes: LiveData<List<Quote>?> = _quotes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _author = MutableLiveData<String?>()
    val author: LiveData<String?> = _author

    private val _dailyAffirmation = MutableLiveData<Quote?>()
    val dailyAffirmation: LiveData<Quote?> = _dailyAffirmation

    private val _keywords = MutableLiveData<List<Keyword>>()
    val keywords: LiveData<List<Keyword>> = _keywords

    fun clearError() {
        _error.value = null
    }

    init {
        loadQuoteOfTheDay() // Initiales Laden des Zitats des Tages
        loadAllQuotesHome() // Initiales Laden aller Zitate
    }


    fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError() // Fehlerstatus zurücksetzen

            try {
                // Versuche, das Zitat des Tages von der API abzurufen
                val quote = repository.getQuoteOfTheDay() ?: repository.getQuoteOfTheDayFromLocal()
                _dailyAffirmation.postValue(quote)
            } catch (e: Exception) {
                // Fange alle Fehler ab und setze die Fehlermeldung
                _error.postValue("Fehler beim Laden des Zitats: ${e.message}")

                _dailyAffirmation.postValue(null)
            } finally {
                _isLoading.postValue(false) // Ladezustand zurücksetzen
            }
        }
    }

    fun loadAllQuotesHome() {
        // Starte einen neuen Coroutine-Block im ViewModelScope
        viewModelScope.launch {
            // Setze den Ladeindikator auf true
            _isLoading.postValue(true)
            try {
                // Versuche, alle Zitate aus dem Repository abzurufen
                val allQuotes = repository.getAllQuotes()

                // Log für die abgerufenen Zitate
                Log.d("QuoteViewModel", "Fetched quotes: $allQuotes")

                // Überprüfe, ob keine Zitate gefunden wurden
                if (allQuotes.isEmpty()) {
                    // Setze eine Fehlermeldung, wenn keine Zitate gefunden wurden
                    _error.postValue("Keine Zitate gefunden.")
                } else {
                    // Setze die abgerufenen Zitate in die LiveData
                    _quotes.postValue(allQuotes)
                }
            } catch (e: Exception) { // Fange alle Fehler ab (Netzwerk und API)
                // Setze die Fehlermeldung für Fehler
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                // Setze den Ladeindikator auf false
                _isLoading.postValue(false)
            }
        }
    }


    fun loadQuotesByKeyword(keyword: String) {
        // Starte einen neuen Coroutine-Block im ViewModelScope
        viewModelScope.launch {
            // Setze den Ladeindikator auf true
            _isLoading.postValue(true) // Ladeindikator starten
            try {
                // Versuche, Zitate nach dem angegebenen Schlüsselwort abzurufen
                val quotes = repository.getQuotesByKeyword(keyword)
                if (quotes.isEmpty()) {
                    // Wenn keine Zitate gefunden werden, setze eine Fehlermeldung
                    _error.postValue("Keine Zitate für das Schlüsselwort '$keyword' gefunden.")
                } else {
                    // Wenn Zitate gefunden werden, setze die Liste der Zitate
                    _quotes.postValue(quotes)
                    _error.postValue(null) // Fehlermeldung zurücksetzen
                }
            } catch (e: IOException) {
                // Fange Netzwerkfehler ab und setze die Fehlermeldung
                _error.postValue("Netzwerkfehler: ${e.message}")
            } catch (e: HttpException) {
                // Fange API-spezifische Fehler ab und setze die Fehlermeldung
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
            } finally {
                // Setze den Ladeindikator auf false
                _isLoading.postValue(false) // Ladeindikator stoppen
            }
        }
    }


    fun searchByAuthor(authorName: String) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeindikator starten
            clearError() // Fehlerstatus zurücksetzen

            if (authorName.isBlank()) {
                _error.postValue("Bitte geben Sie einen gültigen Autorennamen ein.") // Validierung
                _isLoading.postValue(false) // Ladeindikator stoppen
                return@launch
            }

            try {
                val quotes = repository.searchQuotesByAuthor(authorName)
                if (quotes.isEmpty()) {
                    // Fallback: Hole Zitate des Autors aus der lokalen Datenbank
                    val localQuotes = repository.getQuotesByAuthor(authorName)
                    if (localQuotes.isNotEmpty()) {
                        _quotes.postValue(localQuotes)
                        _error.postValue(null) // Fehlermeldung zurücksetzen
                    } else {
                        _error.postValue("Keine Zitate für den Autor '$authorName' gefunden.")
                    }
                } else {
                    _quotes.postValue(quotes)
                    _error.postValue(null) // Fehlermeldung zurücksetzen
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für den Autor: ${e.message}")
            } finally {
                _isLoading.postValue(false) // Ladeindikator stoppen
            }
        }
    }

    fun searchByKeyword(keyword: String) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeindikator starten
            clearError() // Fehlerstatus zurücksetzen

            if (keyword.isBlank()) {
                _error.postValue("Bitte geben Sie ein gültiges Schlüsselwort ein.") // Validierung
                _isLoading.postValue(false) // Ladeindikator stoppen
                return@launch
            }

            try {
                val quotes = repository.getQuotesByKeyword(keyword)
                if (quotes.isEmpty()) {
                    _error.postValue("Keine Zitate für das Schlüsselwort '$keyword' gefunden.")
                } else {
                    _quotes.postValue(quotes)
                    _error.postValue(null) // Fehlermeldung zurücksetzen
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für das Schlüsselwort: ${e.message}")
            } finally {
                _isLoading.postValue(false) // Ladeindikator stoppen
            }
        }
    }

    fun getAuthorByName(authorName: String): LiveData<String?> {
        val authorNameLiveData = MutableLiveData<String?>()

        if (authorName.isBlank()) {
            authorNameLiveData.postValue(null) // Setze auf null, wenn der Autorname ungültig ist
            return authorNameLiveData
        }

        viewModelScope.launch {
            try {
                val author = repository.getAuthorByName(authorName) // Autor abrufen
                authorNameLiveData.postValue(author?.name) // Namen setzen oder null, wenn nicht gefunden
            } catch (e: Exception) {
                authorNameLiveData.postValue(null) // Setze auf null bei einem Fehler
                Log.e("HomeViewModel", "Fehler beim Abrufen des Autors: ${e.message}")
            }
        }
        return authorNameLiveData
    }


}