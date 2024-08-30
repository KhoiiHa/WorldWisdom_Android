package com.example.projektworldwisdom.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseRepository(private val apiService: WorldWisdomApiService) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    suspend fun registerNewUser(email: String, password: String,){
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            loginUser(email, password)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    suspend fun loginUser(email: String, password: String){
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            _currentUser.postValue(firebaseAuth.currentUser)
        }catch (e: Exception){
            e.printStackTrace()


        }
    }

    fun logoutUser(){
        firebaseAuth.signOut()
        _currentUser.value = firebaseAuth.currentUser
    }



}