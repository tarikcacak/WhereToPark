package com.example.wheretopark.ui.credit

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class CreditViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val creditSubject: PublishSubject<String> = PublishSubject.create()

    val currentUid = firebaseAuth.currentUser?.uid.toString()

    fun getUserData() {
        firebaseFirestore.collection("user").whereEqualTo("uid", currentUid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("MapViewModel", "Error fetching user data: ${exception.message}")
                    return@addSnapshotListener
                } else {
                    if (!snapshot!!.isEmpty) {
                        val documentList = snapshot.documents

                        for (document in documentList) {
                            val credits = document.get("credits") as String
                            creditSubject.onNext(credits)
                        }
                    }
                }
            }
    }

    fun updateUserCredits(newCredits: Int) {
        firebaseFirestore.collection("user")
            .document(currentUid)
            .update("credits", newCredits.toString())
            .addOnSuccessListener {}
            .addOnFailureListener { e ->
                Log.e("MapViewModel", "Error updating credits: ${e.message}")
            }
    }

    fun observeCreditState(): Observable<String> {
        return creditSubject.hide()
    }

}