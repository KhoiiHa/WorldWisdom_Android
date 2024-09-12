package com.example.projektworldwisdom.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login() {
        val email = _email.value
        val password = _password.value

        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            _error.value = "E-Mail oder Passwort darf nicht leer sein."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isLoggedIn.value = true
                            _error.value = null
                        } else {
                            _error.value = task.exception?.message
                            _isLoggedIn.value = false
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Fehler beim Anmelden: ${e.message}"
                _isLoading.value = false
                Log.e("LoginViewModel", "Fehler beim Anmelden", e)
            }
        }
    }
}
