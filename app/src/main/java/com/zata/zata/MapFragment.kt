package com.zata.zata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.google.firebase.firestore.FirebaseFirestore
import com.zata.zata.model.ColonyReport
import android.util.Log

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // OSMDroid config
        Configuration.getInstance().userAgentValue =
            requireContext().packageName

        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.osm_map)


        setupMap()
        enableUserLocation()
        loadColoniesFromFirestore()


        return view
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)


        mapView.zoomController.setVisibility(
            org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
        )

        mapView.controller.setZoom(14.0)

        val defaultPoint = GeoPoint(12.9716, 77.5946)
        mapView.controller.setCenter(defaultPoint)
    }


    private fun enableUserLocation() {
        locationOverlay = MyLocationNewOverlay(
            GpsMyLocationProvider(requireContext()),
            mapView
        )
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        mapView.overlays.add(locationOverlay)
    }

    fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.title = title
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
    }
    private fun loadColoniesFromFirestore() {

        FirebaseFirestore.getInstance()
            .collection("colonies")
            .get()
            .addOnSuccessListener { documents ->

                for (doc in documents) {
                    val report = doc.toObject(ColonyReport::class.java)

                    // Show only confident rock-bee colonies
                    if (report.isRockBee && report.confidence >= 0.6f) {

                        Log.d("MapFragment", "Adding bee marker at ${report.lat}, ${report.lng}")


                        val point = GeoPoint(report.lat, report.lng)

// 1️⃣ Create drawable FIRST
                        val drawable = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_bee_marker
                        )

// 2️⃣ Force bounds (VERY IMPORTANT)
                        drawable?.setBounds(0, 0, 96, 96)

// 3️⃣ Create marker
                        val marker = Marker(mapView)
                        marker.position = point
                        marker.title = "Rock Bee Colony"
                        marker.icon = drawable
                        marker.subDescription =
                            "Confidence: ${(report.confidence * 100).toInt()}%"
                        marker.setAnchor(
                            Marker.ANCHOR_CENTER,
                            Marker.ANCHOR_BOTTOM
                        )

// 4️⃣ Add to map
                        mapView.overlays.add(marker)


                    }
                }

                mapView.invalidate()
            }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
