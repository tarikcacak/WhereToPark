package com.example.wheretopark.ui.register

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
import com.example.wheretopark.databinding.FragmentLoginBinding
import com.example.wheretopark.databinding.FragmentRegisterBinding
import com.example.wheretopark.models.user.User
import com.example.wheretopark.util.RegisterValidation
import com.example.wheretopark.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var disposable: Disposable? = null
    private val viewModel: RegisterViewModel by activityViewModels()
    private lateinit var registerDisposable: Disposable
    private lateinit var validationDisposable: Disposable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRegisterButtonClickListener()
        onLoginNowClickListener()
        observeRegister()
        observeValidation()
    }

    private fun onRegisterButtonClickListener() {
        binding.apply {
            btnRegister.setOnClickListener {
                val password = etPassword.text.toString()
                val passwordConf = etPasswordConf.text.toString()
                if (password == passwordConf) {
                    val user = User(
                        etUsername.text.toString().trim(),
                        etEmail.text.toString().trim(),
                        etPassword.text.toString()
                    )
                    viewModel.createAccountWithEmailAndPassword(user)
                } else {
                    binding.layoutPasswordConf.helperText = "Password not matching!"
                }
            }
        }
    }

    private fun observeRegister() {
        registerDisposable = viewModel.observeRegisterSubject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.pbRegister.visibility = View.VISIBLE
                    }
                    is Resource.Error -> {
                        binding.pbRegister.visibility = View.GONE
                        showSnackBar(message = resource.message)
                    }
                    is Resource.Success -> {
                        binding.pbRegister.visibility = View.GONE
                        showSnackBar(message = "Successfully registered!")
                    }
                    else -> Unit
                }
            }
    }

    private fun observeValidation() {
        validationDisposable = viewModel.observeValidationSubject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { validation ->
                if (validation.username is RegisterValidation.Failed) {
                    binding.etUsername.apply {
                        requestFocus()
                        binding.layoutUsername.helperText = validation.username.message
                    }
                }
                if (validation.email is RegisterValidation.Failed) {
                    binding.etEmail.apply {
                        requestFocus()
                        binding.layoutEmail.helperText = validation.email.message
                    }
                }
                if (validation.password is RegisterValidation.Failed) {
                    binding.etPassword.apply {
                        requestFocus()
                        binding.layoutPassword.helperText = validation.password.message
                    }
                }
            }
    }

    private fun onLoginNowClickListener() {
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::registerDisposable.isInitialized && !registerDisposable.isDisposed) {
            registerDisposable.dispose()
        }
        if (::validationDisposable.isInitialized && !validationDisposable.isDisposed) {
            validationDisposable.dispose()
        }
        _binding = null
    }
}