package com.example.wheretopark.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.wheretopark.R
import com.example.wheretopark.activities.LoginRegisterActivity
import com.example.wheretopark.databinding.FragmentProfileBinding
import com.example.wheretopark.models.ticket.Ticket
import com.example.wheretopark.ui.profile.adapter.TicketAdapter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var creditDisposable: Disposable
    private lateinit var userDisposable: Disposable
    private lateinit var pictureDisposable: Disposable
    private lateinit var emailDisposable: Disposable
    private lateinit var platesDisposable: Disposable
    private lateinit var ticketsDisposable: Disposable
    private lateinit var ticketAdapter: TicketAdapter
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var ticketList: List<Ticket>
    private var profilePictureUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserData()
        viewModel.getTicketData()
        observeRxJava()
        setupRecyclerView()
        onTicketLongClickListener()
        pickImage()
        onSignOutClick()
    }

    private fun onTicketLongClickListener() {
        ticketAdapter.onItemLongClickListener { ticket ->
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("End Ticket")
            dialogBuilder.setMessage("Are you sure you want to end ticket at ${ticket.location}, expiring ${formatExpiringDate(ticket.expiring)}")
            dialogBuilder.setCancelable(true)
            dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                viewModel.deleteTicket(ticket)
                dialog.dismiss()
            }
            dialogBuilder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    private fun formatExpiringDate(expiringDate: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd MMM HH:mm:ss", Locale.ENGLISH)

        try {
            val date = inputFormat.parse(expiringDate)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Invalid Date"
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profilePictureUri = result.data?.data
                viewModel.saveImage(profilePictureUri!!)
                Glide.with(this@ProfileFragment)
                    .load(profilePictureUri)
                    .into(binding.ivProfile)
            }
        }

    private fun pickImage() {
        binding.ivProfile.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            activityResultLauncher.launch(intent)
            true
        }
    }

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter()
        binding.rvTickets.apply {
            adapter = ticketAdapter
            layoutManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun observeRxJava() {
        creditDisposable = viewModel.observeCreditState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { credits ->
                binding.tvCredits.text = "Credits: $credits"
            }
        userDisposable = viewModel.observeUserState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { user ->
                binding.tvProfile.text = user
            }
        pictureDisposable = viewModel.observePictureState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { picture ->
                Glide.with(this@ProfileFragment)
                    .load(picture)
                    .into(binding.ivProfile)
            }
        platesDisposable = viewModel.observePlateState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { plates ->
                binding.tvPlates.text = plates
            }
        emailDisposable = viewModel.observeEmailState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { email ->
                binding.tvEmail.text = email
            }
        ticketsDisposable = viewModel.observeTicketsState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { tickets ->
                ticketList = tickets
                checkTicketDateExpiring()
                ticketAdapter.setTickets(tickets as ArrayList<Ticket>)
            }
    }

    private fun checkTicketDateExpiring() {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val currentDate = Calendar.getInstance().time
        val ticketsToRemove = mutableListOf<Ticket>()



        for (ticket in ticketList) {
            val expDate = inputFormat.parse(ticket.expiring)
            if (expDate != null && expDate.before(currentDate)) {
                ticketsToRemove.add(ticket)
            }
        }

        for (ticketToRemove in ticketsToRemove) {
            viewModel.deleteTicket(ticketToRemove)
        }
    }

    private fun onSignOutClick() {
        binding.tvSignOut.setOnClickListener {
            viewModel.logOut()
            Intent(requireContext(), LoginRegisterActivity::class.java).also { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::creditDisposable.isInitialized && !creditDisposable.isDisposed) {
            creditDisposable.dispose()
        }
        if (::userDisposable.isInitialized && !userDisposable.isDisposed) {
            userDisposable.dispose()
        }
        if (::pictureDisposable.isInitialized && !pictureDisposable.isDisposed) {
            pictureDisposable.dispose()
        }
        if (::emailDisposable.isInitialized && !emailDisposable.isDisposed) {
            emailDisposable.dispose()
        }
        if (::platesDisposable.isInitialized && !platesDisposable.isDisposed) {
            platesDisposable.dispose()
        }
        if (::ticketsDisposable.isInitialized && !ticketsDisposable.isDisposed) {
            ticketsDisposable.dispose()
        }
        _binding = null
    }

}