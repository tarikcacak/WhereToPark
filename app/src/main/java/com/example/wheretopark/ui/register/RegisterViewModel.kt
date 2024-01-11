package com.example.wheretopark.ui.register

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airmovies.util.Resource
import com.example.wheretopark.models.user.User
import com.example.wheretopark.util.RegisterFieldState
import com.example.wheretopark.util.RegisterValidation
import com.example.wheretopark.util.validateEmail
import com.example.wheretopark.util.validatePassword
import com.example.wheretopark.util.validatePlates
import com.example.wheretopark.util.validateUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val registerSubject: BehaviorSubject<Resource<User>> = BehaviorSubject.createDefault(Resource.Unspecified())
    private val validationSubject: BehaviorSubject<RegisterFieldState> = BehaviorSubject.create()

    fun createAccountWithEmailAndPassword(user: User) = viewModelScope.launch {
        if (checkValidation(user)) {
            registerSubject.onNext(Resource.Loading())
            firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnSuccessListener {
                    it.user?.let {
                        saveUserInfo(it.uid, user)
                    }
                }
                .addOnFailureListener {
                    registerSubject.onNext(Resource.Error(it.message.toString()))
                }
        } else {
            val registerFieldState = RegisterFieldState(
                validateUser(user.username),
                validateEmail(user.email),
                validatePlates(user.plates),
                validatePassword(user.password)
            )
            validationSubject.onNext(registerFieldState)
        }
    }

    @SuppressLint("CheckResult")
    private fun saveUserInfo(userUid: String, user: User) {
        val hashMap = hashMapOf<String, Any>()
        hashMap["username"] = user.username
        hashMap["email"] = user.email
        hashMap["password"] = user.password
        hashMap["plates"] = user.plates
        hashMap["credits"] = user.credits
        hashMap["imgPath"] = user.imgPath
        hashMap["favorites"] = user.favorites
        hashMap["uid"] = userUid

        firestore.collection("user")
            .document(userUid)
            .set(hashMap)
            .addOnSuccessListener {
                registerSubject.onNext(Resource.Success(user))
            }
            .addOnFailureListener {
                registerSubject.onNext(Resource.Error(it.message.toString()))
            }
    }
    private fun checkValidation(user: User): Boolean {
        val emailValidaiton = validateEmail(user.email)
        val passwordValidation = validatePassword(user.password)
        val userValidation = validateUser(user.username)
        val platesValidation = validatePlates(user.plates)
        val shouldRegister = emailValidaiton is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success && userValidation is RegisterValidation.Success &&
                platesValidation is RegisterValidation.Success

        return shouldRegister
    }

    fun observeRegisterSubject(): Observable<Resource<User>> {
        return registerSubject
    }

    fun observeValidationSubject(): Observable<RegisterFieldState> {
        return validationSubject
    }
}