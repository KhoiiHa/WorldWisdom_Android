package com.example.projektworldwisdom.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    private val _downloading = MutableLiveData(false)
    val downloading: LiveData<Boolean> = _downloading

    private val _currentUser = MutableLiveData<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    suspend fun registerNewUser(email: String, password: String) {
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            loginUser(email, password)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun loginUser(email: String, password: String) {
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            _currentUser.postValue(firebaseAuth.currentUser)
        } catch (e: Exception) {
            e.printStackTrace()


        }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
        _currentUser.value = firebaseAuth.currentUser
    }

    fun saveQuote(quoteId: Int) {
        firebaseAuth.currentUser?.let { user ->
            val documentRef = firebaseFirestore
                .collection("USER_Collections")
                .document(user.uid)
                .collection("SavedQuotes")
                .document(quoteId.toString())
            documentRef.set(
                mapOf(
                    "quoteId" to quoteId
                )
            )
        }
    }

    suspend fun getUserCollection(): List<Int>? {
        _downloading.postValue(true)
        firebaseAuth.currentUser?.let { user ->
            val collection = mutableListOf<Int>()
            firebaseFirestore
                .collection("USER_Collections")
                .document(user.uid)
                .collection("SavedQuotes")
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach {
                        collection.add(
                            (it["quoteId"] as Long).toInt()
                        )
                    }
                    _downloading.postValue(false)
                }.await()
            return collection
        }
        return null
    }

}