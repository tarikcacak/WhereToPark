package com.example.wheretopark.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.airmovies.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class MapViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val creditSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")

    val currentUid = firebaseAuth.currentUser?.uid.toString()

    fun getData() {
        firebaseFirestore.collection("user").whereEqualTo("uid", currentUid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("MapViewModel", "Error fetching data: ${exception.message}")
                    return@addSnapshotListener
                }

                if (!snapshot!!.isEmpty) {
                    val documentList = snapshot.documents

                    for (document in documentList) {
                        val credits = document.get("credits") as String
                        creditSubject.onNext(credits)
                    }
                }
            }
    }

    fun observeCreditState(): Observable<String> {
        return creditSubject
    }
}