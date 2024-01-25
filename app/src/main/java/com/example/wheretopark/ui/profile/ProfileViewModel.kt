package com.example.wheretopark.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.airmovies.util.Resource
import com.example.wheretopark.models.ticket.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {
    private val creditSubject: PublishSubject<String> = PublishSubject.create()
    private val userSubject: PublishSubject<String> = PublishSubject.create()
    private val pictureSubject: PublishSubject<String> = PublishSubject.create()
    private val emailSubject: PublishSubject<String> = PublishSubject.create()
    private val platesSubject: PublishSubject<String> = PublishSubject.create()
    private val ticketsSubject: PublishSubject<List<Ticket>> = PublishSubject.create()
    private val ticketSubject: BehaviorSubject<Resource<Ticket>> = BehaviorSubject.createDefault(Resource.Unspecified())

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
                            val picture = document.get("imgPath") as String
                            val email = document.get("email") as String
                            val plates = document.get("plates") as String
                            creditSubject.onNext(credits)
                            userSubject.onNext(user)
                            pictureSubject.onNext(picture)
                            emailSubject.onNext(email)
                            platesSubject.onNext(plates)
                            Log.d("ProfileViewModel", picture)
                        }
                    }
                }
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
                        ticketsSubject.onNext(ticketList)
                    }
                }
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

    fun saveImage(pickedImage: Uri) {
        firebaseStorage.reference.child("imgPath").child(currentUid!!).delete()
        val storage = firebaseStorage.reference.child("imgPath").child(currentUid)
        storage.putFile(pickedImage)
        storage.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()

            firebaseFirestore.collection("user").document(currentUid)
                .update(
                    mapOf(
                        "imgPath" to imageUrl
                    )
                )
        }
    }

    fun logOut(){
        firebaseAuth.signOut()
    }

    fun observeCreditState(): Observable<String> {
        return creditSubject.hide()
    }

    fun observeUserState(): Observable<String> {
        return userSubject.hide()
    }

    fun observePictureState(): Observable<String> {
        return pictureSubject.hide()
    }

    fun observeEmailState(): Observable<String> {
        return emailSubject.hide()
    }

    fun observePlateState(): Observable<String> {
        return platesSubject.hide()
    }

    fun observeTicketsState(): Observable<List<Ticket>> {
        return ticketsSubject.hide()
    }
}