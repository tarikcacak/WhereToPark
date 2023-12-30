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
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.math.log

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var disposable: Disposable? = null
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
                viewModel.validate(email, password)
                viewModel.login(email, password)
                binding.pbLogin.visibility = View.VISIBLE
            }
        }
    }

    private fun onRegisterNowClickListener() {
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    @SuppressLint("CheckResult")
    private fun observeValidation() {
        disposable = viewModel.observeValidationRxJava()
            .subscribe(
                { loginFieldState ->
                    if (loginFieldState.email is RegisterValidation.Failed) {
                        binding.etEmail.apply {
                            requestFocus()
                            binding.layoutEmail.helperText = loginFieldState.email.message
                        }
                    }
                    if (loginFieldState.password is RegisterValidation.Failed) {
                        binding.etPassword.apply {
                            requestFocus()
                            binding.layoutPassword.helperText = loginFieldState.password.message
                        }
                    }
                },
                { throwable ->
                    showSnackBar(message = throwable.message)
                }
            )
    }

    @SuppressLint("CheckResult")
    private fun observeLogin() {
        disposable = viewModel.observeLoginRxJava()
            .subscribe(
                {
                    binding.pbLogin.visibility = View.GONE
                    showSnackBar(message = "Login successful!")
                    Intent(requireActivity(), MainActivity::class.java).also { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                },
                { throwable ->
                    binding.pbLogin.visibility = View.GONE
                    showSnackBar(message = throwable.message)
                }
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
    }
}