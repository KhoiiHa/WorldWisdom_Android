package com.example.projektworldwisdom.home

import androidx.lifecycle.*
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Keyword
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

    private val _authors = MutableLiveData<List<Author>>()
    val authors: LiveData<List<Author>> = _authors

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
                // Fallback: Hole das Zitat des Tages aus der lokalen Datenbank
                val localQuote = repository.getQuoteOfTheDayFromLocal()
                _dailyAffirmation.postValue(localQuote)
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
                // Überprüfe, ob keine Zitate gefunden wurden
                if (allQuotes.isEmpty()) {
                    // Setze eine Fehlermeldung, wenn keine Zitate gefunden wurden
                    _error.postValue("Keine Zitate gefunden.")
                } else {
                    // Setze die abgerufenen Zitate in die LiveData
                    _quotes.postValue(allQuotes)
                }
            } catch (e: IOException) { // Fange Netzwerkfehler ab
                // Setze die Fehlermeldung für Netzwerkfehler
                _error.postValue("Netzwerkfehler: ${e.message}")
            } catch (e: HttpException) { // Fange API-spezifische Fehler ab
                // Setze die Fehlermeldung für API-Fehler
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
            } finally {
                // Setze den Ladeindikator auf false
                _isLoading.postValue(false)
                // Setze die Fehlermeldung zurück
                _error.postValue(null)
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


    fun loadKeywords() {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeindikator starten
            try {
                // Versuche, die Schlüsselwörter aus dem Repository abzurufen
                val result = repository.getKeywords()
                _keywords.postValue(result) // Setze die abgerufenen Schlüsselwörter
                _error.postValue(null) // Fehlernachricht zurücksetzen
            } catch (e: Exception) {
                // Bei einem Fehler setze die Fehlermeldung
                _error.postValue("Fehler beim Laden der Schlüsselwörter: ${e.message}")
            } finally {
                _isLoading.postValue(false) // Ladeindikator stoppen
            }
        }
    }

    fun searchByAuthor(authorName: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val quotes = repository.searchQuotesByAuthor(authorName)
                _quotes.postValue(quotes)
                if (quotes.isEmpty()) {
                    _error.postValue("Keine Zitate für den Autor '$authorName' gefunden.")
                } else {
                    _error.postValue(null)
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für den Autor: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadAuthors() {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeindikator starten
            try {
                val authors = repository.fetchAuthors() // Autoren abrufen
                _authors.postValue(authors) // Autoren in der LiveData speichern
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Autoren: ${e.message}")
            } finally {
                _isLoading.postValue(false) // Ladeindikator stoppen
            }
        }
    }

    fun getAuthorByName(authorName: String): LiveData<String?> {
        val authorNameLiveData = MutableLiveData<String?>()
        viewModelScope.launch {
            val author = repository.getAuthorByName(authorName) // Hier den Namen verwenden
            authorNameLiveData.postValue(author?.name)
        }
        return authorNameLiveData
    }



}