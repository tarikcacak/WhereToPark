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
    private val ticketListSubject: PublishSubject<List<Ticket>> = PublishSubject.create()


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
        hashMap["uid"] = currentUid

        firebaseFirestore.collection("ticket")
            .add(hashMap)
            .addOnSuccessListener {
                ticketSubject.onNext(Resource.Success(ticket))
            }
            .addOnFailureListener {
                ticketSubject.onNext(Resource.Error(it.message.toString()))
            }

    }

    fun getTicketData() {
        firebaseFirestore.collection("ticket").whereEqualTo("uid", currentUid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("MapViewModel", "Error fetching ticket data: ${exception.message}")
                    return@addSnapshotListener
                } else {
                    if (!snapshot!!.isEmpty) {
                        val ticketList = mutableListOf<Ticket>()

                        for (document in snapshot.documents) {
                            val location = document.getString("location") ?: ""
                            val expiring = document.getString("expiring") ?: ""
                            val uid = document.getString("uid") ?: ""

                            val ticket = Ticket(location, expiring, uid)
                            ticketList.add(ticket)
                        }
                        ticketListSubject.onNext(ticketList)
                    }
                }
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

    fun deleteTicket(ticket: Ticket) {
        val location = ticket.location

        firebaseFirestore.collection("ticket")
            .whereEqualTo("uid", currentUid)
            .whereEqualTo("location", location)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("MapViewModel", "Error deleting ticket: ${exception.message}")
                    return@addSnapshotListener
                } else {
                    if (!snapshot!!.isEmpty) {
                        for (document in snapshot.documents) {
                            val uid = document.getString("uid")
                            val loc = document.getString("location")
                            if (uid == currentUid && loc == location) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        ticketSubject.onNext(Resource.Success(ticket.copy()))
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MapViewModel", "Error deleting ticket: ${e.message}")
                                        ticketSubject.onNext(Resource.Error("Error deleting ticket"))
                                    }
                            }
                        }
                    }
                }
            }
    }



    fun observeCreditState(): Observable<String> {
        return creditSubject.hide()
    }

    fun observeUserState(): Observable<String> {
        return userSubject.hide()
    }

    fun observeTicketsState(): Observable<List<Ticket>> {
        return ticketListSubject.hide()
    }
}