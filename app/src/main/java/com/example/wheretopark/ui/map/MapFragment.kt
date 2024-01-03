package com.example.wheretopark.ui.map

import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wheretopark.R
import com.example.wheretopark.databinding.FragmentMapBinding
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(layoutInflater, container, false)

        val ctx = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID)
        val map = binding.mapView
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

        val mGpsLocationProvider = GpsMyLocationProvider(activity)
        val mLocationOverlay = MyLocationNewOverlay(mGpsLocationProvider, map)
        mLocationOverlay.enableMyLocation()
        mLocationOverlay.enableFollowLocation()

        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_menu_compas)
        mLocationOverlay.setPersonIcon(icon)
        map.getOverlays().add(mLocationOverlay)

        mLocationOverlay.runOnFirstFix {
            map.getOverlays().clear()
            map.getOverlays().add(mLocationOverlay)
            mapController.animateTo(mLocationOverlay.myLocation)
        }
        return binding.root
    }

}