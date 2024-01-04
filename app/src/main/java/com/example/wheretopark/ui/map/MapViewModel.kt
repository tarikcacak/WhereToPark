package com.example.wheretopark.ui.map

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
    val creditState: Observable<String> = creditSubject.hide()

    fun updateProgressState(newState: String) {
        creditSubject.onNext(newState)
    }
}