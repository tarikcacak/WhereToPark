package com.example.wheretopark.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.wheretopark.R
import com.example.wheretopark.databinding.FragmentMapBinding
import com.example.wheretopark.util.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var parkingOverlay: ItemizedIconOverlay<OverlayItem>

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
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    // Handle marker click if needed
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
        val options = arrayOf("Pay 2h", "Pay 5h", "Pay 1 day", "Pay 1 month", "Cancel")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(parkingName)
            .setItems(options) { _, which ->
                when (which) {

                }
            }
    }

    private fun addParkingToMap(map: MapView) {
        val parkingOmega = OverlayItem("Parking Omega", "Description", GeoPoint(44.5384215, 18.6633669))
        val parkingBCC = OverlayItem("Parking BCC", "Description", GeoPoint(44.5326340, 18.6532643))
        val parkingGradski = OverlayItem("Gradski Parking", "Description", GeoPoint(44.5407361, 18.6767803))
        val parkingHotelTuzla = OverlayItem("Parking Hotel Tuzla", "Description", GeoPoint(44.5314820, 18.6888965))
        val parkingMellain = OverlayItem("Parking Mellain", "Description", GeoPoint(44.534133, 18.687450))
        val parkingBelamionix = OverlayItem("Parking Belamionix", "Description", GeoPoint(44.525375, 18.628034))

        parkingOverlay.addItem(parkingOmega)
        parkingOverlay.addItem(parkingBCC)
        parkingOverlay.addItem(parkingGradski)
        parkingOverlay.addItem(parkingHotelTuzla)
        parkingOverlay.addItem(parkingMellain)
        parkingOverlay.addItem(parkingBelamionix)

        map.overlays.add(parkingOverlay)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}