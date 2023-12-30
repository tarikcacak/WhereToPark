package com.example.wheretopark.ui.login

import androidx.lifecycle.ViewModel
import com.example.airmovies.util.Resource
import com.example.wheretopark.util.LoginFieldState
import com.example.wheretopark.util.RegisterValidation
import com.example.wheretopark.util.validateEmail
import com.example.wheretopark.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val loginSubject: BehaviorSubject<Resource<FirebaseUser>> = BehaviorSubject.createDefault(Resource.Unspecified())
    private val validationSubject: BehaviorSubject<LoginFieldState> = BehaviorSubject.create()

    fun login(email: String, password: String) {
        if (checkValidation(email, password)) {
            loginSubject.onNext(Resource.Loading())
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        loginSubject.onNext(Resource.Success(it))
                    }
                }
                .addOnFailureListener {
                    loginSubject.onNext(Resource.Error(it.message.toString()))
                }
        } else {
            val loginFieldState = LoginFieldState(
                validateEmail(email),
                validatePassword(password)
            )
            validationSubject.onNext(loginFieldState)
        }
    }

    private fun checkValidation(email: String, password: String): Boolean {
        val emailValidaiton = validateEmail(email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidaiton is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success

        return shouldRegister
    }

    fun observeLoginSubject(): Observable<Resource<FirebaseUser>> {
        return loginSubject
    }

    fun observeValidationSubject(): Observable<LoginFieldState> {
        return validationSubject
    }

}