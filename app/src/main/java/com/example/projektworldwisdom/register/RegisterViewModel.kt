package com.example.projektworldwisdom.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isRegistered = MutableLiveData<Boolean>()
    val isRegistered: LiveData<Boolean> = _isRegistered

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register() {
        val email = _email.value
        val password = _password.value
        val confirmPassword = _confirmPassword.value

        if (email.isNullOrEmpty() || password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
            _error.value = "E-Mail und Passwort dürfen nicht leer sein."
            return
        }

        if (password != confirmPassword) {
            _error.value = "Passwörter stimmen nicht überein."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isRegistered.value = true
                            _error.value = null
                        } else {
                            _error.value = task.exception?.message
                            _isRegistered.value = false
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Fehler bei der Registrierung: ${e.message}"
                _isLoading.value = false
                Log.e("RegisterViewModel", "Fehler bei der Registrierung", e)
            }
        }
    }
}