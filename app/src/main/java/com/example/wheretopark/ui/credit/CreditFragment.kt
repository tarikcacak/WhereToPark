package com.example.wheretopark.ui.credit

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.wheretopark.R
import com.example.wheretopark.databinding.FragmentCreditBinding
import com.example.wheretopark.util.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.delay

@AndroidEntryPoint
class CreditFragment : Fragment() {

    private var _binding: FragmentCreditBinding? = null
    private val binding get() = _binding!!
    private lateinit var creditDisposable: Disposable
    private val viewModel: CreditViewModel by activityViewModels()
    private lateinit var currentCredits: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreditBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserData()
        observeCredits()
        onGoldClickListener()
        onSilverClickListener()
        onBronzeClickListener()
    }

    private fun onGoldClickListener() {
        binding.cvOptionOne.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Gold Pack")
                .setMessage("Are you sure you want to buy the Gold Pack")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, which ->
                    val credits = 100
                    var curCredits = currentCredits.toInt()
                    curCredits += credits
                    currentCredits = curCredits.toString()
                    viewModel.updateUserCredits(curCredits)
                    observeCredits()
                    showSnackBar("Successfully bought the Gold Pack!")
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun onSilverClickListener() {
        binding.cvOptionTwo.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Silver Pack")
                .setMessage("Are you sure you want to buy the Silver Pack")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, which ->
                    val credits = 50
                    var curCredits = currentCredits.toInt()
                    curCredits += credits
                    currentCredits = curCredits.toString()
                    viewModel.updateUserCredits(curCredits)
                    observeCredits()
                    showSnackBar("Successfully bought the Silver Pack!")
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun onBronzeClickListener() {
        binding.cvOptionThree.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Bronze Pack")
                .setMessage("Are you sure you want to buy the Bronze Pack")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, which ->
                    val credits = 10
                    var curCredits = currentCredits.toInt()
                    curCredits += credits
                    currentCredits = curCredits.toString()
                    viewModel.updateUserCredits(curCredits)
                    observeCredits()
                    showSnackBar("Successfully bought the Bronze Pack!")
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeCredits() {
        creditDisposable = viewModel.observeCreditState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { credits ->
                currentCredits = credits
                binding.tvCredits.text = "Credits: $credits"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::creditDisposable.isInitialized && !creditDisposable.isDisposed) {
            creditDisposable.dispose()
        }
        _binding = null
    }
}