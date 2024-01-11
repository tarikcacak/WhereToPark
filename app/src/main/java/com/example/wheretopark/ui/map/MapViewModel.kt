package com.example.wheretopark.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.airmovies.util.Resource
import com.example.wheretopark.models.ticket.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val creditSubject: PublishSubject<String> = PublishSubject.create()
    private val userSubject: PublishSubject<String> = PublishSubject.create()
    private val ticketSubject: BehaviorSubject<Resource<Ticket>> = BehaviorSubject.createDefault(Resource.Unspecified())

    val currentUid = firebaseAuth.currentUser?.uid.toString()

    fun getData() {
        firebaseFirestore.collection("user").whereEqualTo("uid", currentUid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("MapViewModel", "Error fetching data: ${exception.message}")
                    return@addSnapshotListener
                } else {
                    if (!snapshot!!.isEmpty) {
                        val documentList = snapshot.documents

                        for (document in documentList) {
                            val credits = document.get("credits") as String
                            val user = document.get("username") as String
                            if (credits != null && user != null) {
                                creditSubject.onNext(credits)
                                userSubject.onNext(user)
                            } else {
                                Log.w("MapViewModel", "Credits is null in document")
                            }
                        }
                    }
                }
            }
    }

    fun saveTicketInfo(ticket: Ticket) {
        val hashMap = hashMapOf<String, Any>()
        hashMap["location"] = ticket.location
        hashMap["expiring"] = ticket.expiring
        hashMap["user"] = ticket.user

        firebaseFirestore.collection("ticket")
            .add(hashMap)
            .addOnSuccessListener {
                ticketSubject.onNext(Resource.Success(ticket))
            }
            .addOnFailureListener {
                ticketSubject.onNext(Resource.Error(it.message.toString()))
            }

    }

    fun updateUserCredits(newCredits: Int) {
        if (currentUid != null) {
            firebaseFirestore.collection("user")
                .document(currentUid)
                .update("credits", newCredits.toString())
                .addOnSuccessListener {}
                .addOnFailureListener { e ->
                    Log.e("MapViewModel", "Error updating credits: ${e.message}")
                }
        } else {
            Log.e("MapViewModel", "User UID is null")
        }
    }

    fun observeCreditState(): Observable<String> {
        return creditSubject.hide()
    }

    fun observeUserState(): Observable<String> {
        return userSubject.hide()
    }
}