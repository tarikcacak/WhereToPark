package com.example.wheretopark.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.airmovies.util.Resource
import com.example.wheretopark.R
import com.example.wheretopark.activities.MainActivity
import com.example.wheretopark.databinding.FragmentLoginBinding
import com.example.wheretopark.util.RegisterValidation
import com.example.wheretopark.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.math.log

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginDisposable: Disposable
    private lateinit var validationDisposable: Disposable
    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onLoginButtonClickListener()
        observeLogin()
        observeValidation()
        onRegisterNowClickListener()
    }

    private fun onLoginButtonClickListener() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                viewModel.login(email, password)
            }
        }
    }

    private fun observeLogin() {
        loginDisposable = viewModel.observeLoginSubject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.pbLogin.visibility = View.VISIBLE
                    }
                    is Resource.Error -> {
                        binding.pbLogin.visibility = View.GONE
                        showSnackBar(message = resource.message)
                    }
                    is Resource.Success -> {
                        binding.pbLogin.visibility = View.GONE
                        Intent(requireActivity(), MainActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        showSnackBar(message = "Login successful!")
                    }
                    else -> Unit
                }
            }
    }

    private fun observeValidation() {
        validationDisposable = viewModel.observeValidationSubject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    binding.layoutEmail.apply {
                        requestFocus()
                        binding.layoutEmail.helperText = validation.email.message
                    }
                }
                if (validation.password is RegisterValidation.Failed) {
                    binding.layoutPassword.apply {
                        requestFocus()
                        binding.layoutPassword.helperText = validation.password.message
                    }
                }
            }
    }

    private fun onRegisterNowClickListener() {
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::loginDisposable.isInitialized && !loginDisposable.isDisposed) {
            loginDisposable.dispose()
        }
        if (::validationDisposable.isInitialized && !validationDisposable.isDisposed) {
            validationDisposable.dispose()
        }
        _binding = null
    }
}