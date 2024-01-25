package com.example.wheretopark.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.wheretopark.databinding.FragmentMapBinding
import com.example.wheretopark.models.ticket.Ticket
import com.example.wheretopark.util.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var creditDisposable: Disposable
    private lateinit var userDisposable: Disposable
    private lateinit var ticketsDisposable: Disposable
    private val viewModel: MapViewModel by activityViewModels()
    private lateinit var parkingOverlay: ItemizedIconOverlay<OverlayItem>
    private lateinit var username: String
    private lateinit var currentCredits: String
    private lateinit var ticketList: List<Ticket>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        parkingOverlay = createParkingOverlay()
        initializeMap()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndRequestPermissions()
        viewModel.getUserData()
        viewModel.getTicketData()
        observeRxJava()
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun createParkingOverlay(): ItemizedIconOverlay<OverlayItem> {
        return ItemizedIconOverlay(
            ArrayList(),
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                    showParkingOptionsDialog(item)
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    // Handle long press on marker if needed
                    return false
                }
            },
            requireContext()
        )
    }

    private fun initializeMap() {
        val ctx = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        val map = binding.mapView
        map.tileProvider.clearTileCache()
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val mapController: IMapController
        mapController = map.getController()
        mapController.setZoom(18)

        val cityLatitude = 44.5376
        val cityLongitude = 18.6694

        val cityGeoPoint = GeoPoint(cityLatitude, cityLongitude)
        mapController.setCenter(cityGeoPoint)

        addParkingToMap(map)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeMap()
            } else {
                showSnackBar(message = "Permission not granted!")
            }
        }
    }

    private fun showParkingOptionsDialog(overlayItem: OverlayItem) {
        val parkingName = overlayItem.title
        val options = arrayOf("Pay 2h (2 credits)", "Pay 5h (4 credits)", "Pay 1 day (10 credits)", "Pay 1 month (100 credits)", "Cancel")
        var hasTicket: Boolean = false
        for (ticket in ticketList) {
            if (ticket.location == parkingName) {
                val expiring = formatExpiringDate(ticket.expiring)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(parkingName)
                    .setMessage("You currently have a ticket on this parking that is expiring: $expiring")
                    .setCancelable(true)
                    .setPositiveButton("End") { dialog, which ->

                    }
                hasTicket = true
            }
        }
        binding.tvCredits.visibility = View.VISIBLE

        if (hasTicket) {
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(parkingName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> handlePaymentOption("Pay 2h", overlayItem)
                    1 -> handlePaymentOption("Pay 5h", overlayItem)
                    2 -> handlePaymentOption("Pay 1 day", overlayItem)
                    3 -> handlePaymentOption("Pay 1 month", overlayItem)
                    4 -> binding.tvCredits.visibility = View.GONE
                }
            }
            .show()
    }

    private fun handlePaymentOption(option: String, overlayItem: OverlayItem) {
        val calendar = Calendar.getInstance()

        when (option) {
            "Pay 2h" -> {
                if (currentCredits.toInt() < 2)
                    showSnackBar("You don't have enough credits!")
                else
                    paymentFunction(1, overlayItem, calendar, 2)
                binding.tvCredits.visibility = View.GONE
            }
            "Pay 5h" -> {
                if (currentCredits.toInt() < 4)
                    showSnackBar("You don't have enough credits!")
                else
                    paymentFunction(5, overlayItem, calendar, 4)
                binding.tvCredits.visibility = View.GONE
            }
            "Pay 1 day" -> {
                if (currentCredits.toInt() < 10)
                    showSnackBar("You don't have enough credits!")
                else
                    paymentFunction(24, overlayItem, calendar, 10)
                binding.tvCredits.visibility = View.GONE
            }
            "Pay 1 month" -> {
                if (currentCredits.toInt() < 100)
                    showSnackBar("You don't have enough credits!")
                else
                    paymentFunction(30*24, overlayItem, calendar, 100)
                binding.tvCredits.visibility = View.GONE
            }
        }
    }

    private fun paymentFunction(time: Int, overlayItem: OverlayItem, calendar: Calendar, credits: Int) {
        val timeStr: String = when (time) {
            24 -> "1 day"
            30*24 -> "1 month"
            else -> "${time}h"
        }
        showSnackBar(message = "$timeStr ticket transaction successful!")
        val location = overlayItem.title
        val expiring = calculateExpirationTime(calendar, 30*24)
        val user = username
        val ticket = Ticket(
            location,
            expiring.toString(),
            user
        )
        val newCredits = currentCredits.toInt() - credits
        viewModel.updateUserCredits(newCredits)
        viewModel.saveTicketInfo(ticket)
    }

    private fun calculateExpirationTime(calendar: Calendar, hoursToAdd: Int): Date {
        calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd)
        return calendar.time
    }

    private fun observeRxJava() {
        creditDisposable = viewModel.observeCreditState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { credits ->
                currentCredits = credits
                binding.tvCredits.text = "Credits: $credits"
            }
        userDisposable = viewModel.observeUserState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { user ->
                username = user
            }
        ticketsDisposable = viewModel.observeTicketsState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { tickets ->
                ticketList = tickets
            }
    }



    private fun addParkingToMap(map: MapView) {
        val parkingOmega = OverlayItem("Parking Omega", "Description", GeoPoint(44.5384215, 18.6633669))
        val parkingBCC = OverlayItem("Parking BCC", "Description", GeoPoint(44.5326340, 18.6532643))
        val parkingGradski = OverlayItem("Gradski Parking", "Description", GeoPoint(44.5407361, 18.6767803))
        val parkingHotelTuzla = OverlayItem("Parking Hotel Tuzla", "Description", GeoPoint(44.5314820, 18.6888965))
        val parkingMellain = OverlayItem("Parking Mellain", "Description", GeoPoint(44.534133, 18.687450))
        val parkingBelamionix = OverlayItem("Parking Belamionix", "Description", GeoPoint(44.525375, 18.628034))
        val parkingSkver = OverlayItem("Parking Skver", "Description", GeoPoint(44.540804, 18.673156))
        val parkingTrgSlobode = OverlayItem("Parking Trg Slobode", "Description", GeoPoint(44.539912, 18.676031))
        val parkingTuzla = OverlayItem("Parking Tuzla", "Description", GeoPoint(44.540185, 18.675012))
        val parkingPanonica = OverlayItem("Parking Panonica", "Description", GeoPoint(44.538399, 18.683061))
        val parking15Maj = OverlayItem("Parking 15 Maj", "Description", GeoPoint(44.533129, 18.696312))
        val parkingMercator = OverlayItem("Parking 15 Maj", "Description", GeoPoint(44.533135, 18.682501))

        parkingOverlay.addItem(parkingOmega)
        parkingOverlay.addItem(parkingBCC)
        parkingOverlay.addItem(parkingGradski)
        parkingOverlay.addItem(parkingHotelTuzla)
        parkingOverlay.addItem(parkingMellain)
        parkingOverlay.addItem(parkingBelamionix)
        parkingOverlay.addItem(parkingSkver)
        parkingOverlay.addItem(parkingTrgSlobode)
        parkingOverlay.addItem(parkingTuzla)
        parkingOverlay.addItem(parkingPanonica)
        parkingOverlay.addItem(parking15Maj)
        parkingOverlay.addItem(parkingMercator)

        map.overlays.add(parkingOverlay)
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

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::creditDisposable.isInitialized && !creditDisposable.isDisposed) {
            creditDisposable.dispose()
        }
        _binding = null
    }

}