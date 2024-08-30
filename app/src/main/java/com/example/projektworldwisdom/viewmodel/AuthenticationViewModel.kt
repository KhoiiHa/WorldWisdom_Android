package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.remote.FirebaseRepository
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()

    val currentUser = firebaseRepository.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            firebaseRepository.loginUser(email, password)
        }
    }

    fun register(email: String, password: String,firstname: String, lastname: String) {
        viewModelScope.launch {
            firebaseRepository.registerNewUser(email, password)
        }
    }

    fun logout() {
        firebaseRepository.logoutUser()
    }


}