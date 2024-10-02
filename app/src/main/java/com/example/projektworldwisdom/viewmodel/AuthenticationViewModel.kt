package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.remote.FirebaseRepository
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    val currentUser = firebaseRepository.currentUser

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.loginUser(email, password)
            } catch (e: Exception) {
                _loginError.value = "Anmeldung fehlgeschlagen: ${e.message}"
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.registerNewUser(email, password)
            }catch (e: Exception) {
                _loginError.value = "Regestrierung fehlgeschlagen: ${e.message}"
            }

        }
    }

    fun logout() {
        firebaseRepository.logoutUser()
    }


}