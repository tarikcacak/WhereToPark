package com.example.wheretopark.ui.login

import androidx.lifecycle.ViewModel
import com.example.wheretopark.util.LoginFieldState
import com.example.wheretopark.util.RegisterValidation
import com.example.wheretopark.util.validateEmail
import com.example.wheretopark.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val loginSubject = PublishSubject.create<Pair<String, String>>()
    private val validationSubject = PublishSubject.create<Pair<String, String>>()

    private val loginObservable: Completable = loginSubject
        .flatMapCompletable { (email, password) ->
            Completable.create { emitter ->
                if (checkValidation(email, password)) {
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            it.user?.let { user ->
                                emitter.onComplete()
                            }
                        }
                        .addOnFailureListener { error ->
                            emitter.onError(error)
                        }
                } else {
                    emitter.onComplete()
                }
            }
        }
        .subscribeOn(Schedulers.io())

    private val validationObservable: Observable<LoginFieldState> = validationSubject
        .flatMap { (email, password) ->
            Observable.create<LoginFieldState> { emitter ->
                val loginFieldState = LoginFieldState(
                    validateEmail(email),
                    validatePassword(password)
                )
                emitter.onNext(loginFieldState)
                emitter.onComplete()
            }
        }
        .subscribeOn(Schedulers.io())

    fun login(email: String, password: String) {
        loginSubject.onNext(Pair(email, password))
    }

    fun observeLoginRxJava(): Completable {
        return loginObservable
    }

    fun validate(email: String, password: String) {
        validationSubject.onNext(Pair(email, password))
    }

    fun observeValidationRxJava(): Observable<LoginFieldState> {
        return validationObservable
    }

    private fun checkValidation(email: String, password: String): Boolean {
        val emailValidaiton = validateEmail(email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidaiton is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success

        return shouldRegister
    }

}