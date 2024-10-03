package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                // Fehlerbehandlung: Exception im Log ausgeben
                e.printStackTrace()
            }
        }
    }
}
