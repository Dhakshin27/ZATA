package com.zata.zata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.zata.zata.model.ColonyReport
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    // 🔥 IMPORTANT: store markers for navigation
    private val colonyMarkers = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // OSMDroid configuration
        Configuration.getInstance().userAgentValue =
            requireContext().packageName

        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.osm_map)

        setupMap()
        enableUserLocation()
        loadColoniesFromFirestore()

        // OPTIONAL (only if FABs exist in fragment_map.xml)
        view.findViewById<View?>(R.id.fab_my_location)?.setOnClickListener {
            centerOnUser()
        }

        view.findViewById<View?>(R.id.fab_nearest)?.setOnClickListener {
            navigateToNearestColony()
        }

        return view
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // ❌ Remove + / − zoom buttons
        mapView.zoomController.setVisibility(
            CustomZoomButtonsController.Visibility.NEVER
        )

        mapView.controller.setZoom(14.0)

        // Default fallback (Bangalore)
        mapView.controller.setCenter(
            GeoPoint(12.9716, 77.5946)
        )
    }

    private fun enableUserLocation() {
        locationOverlay = MyLocationNewOverlay(
            GpsMyLocationProvider(requireContext()),
            mapView
        )

        // 🔹 Convert drawable to bitmap for OSMDroid
        val drawable = requireContext().getDrawable(R.drawable.ic_user_location)!!
        val bitmap = drawableToBitmap(drawable)

        locationOverlay.setPersonIcon(bitmap)

        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        mapView.overlays.add(locationOverlay)
    }

    // 🔹 Utility function to convert drawable to bitmap
    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): android.graphics.Bitmap {
        if (drawable is android.graphics.drawable.BitmapDrawable) {
            drawable.bitmap?.let { return it }
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 64

        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    private fun loadColoniesFromFirestore() {

        FirebaseFirestore.getInstance()
            .collection("colonies")
            .get()
            .addOnSuccessListener { documents ->

                colonyMarkers.clear()
                mapView.overlays.removeAll { it is Marker }

                val points = mutableListOf<GeoPoint>()

                for (doc in documents) {
                    val report = doc.toObject(ColonyReport::class.java)

                    // ✅ Show only confident rock-bee colonies
                    if (report.rockBee && report.confidence >= 0.6) {

                        val point = GeoPoint(report.lat, report.lng)
                        points.add(point)

                        val marker = Marker(mapView).apply {
                            position = point
                            title = "Rock Bee Colony"
                            subDescription =
                                "Confidence: ${(report.confidence * 100).toInt()}%"

                            icon = getScaledMarker(
                                R.drawable.ic_bee_marker,
                                24 // 👈 ideal size (dp)
                            )


                            setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )
                        }


                        colonyMarkers.add(marker)
                        mapView.overlays.add(marker)
                    }
                }

                // 🔥 Auto-fit all colonies
                zoomToAllMarkers(points)
                mapView.invalidate()
            }
    }

    // 🎯 Auto zoom to all colonies
    private fun zoomToAllMarkers(points: List<GeoPoint>) {
        if (points.isEmpty()) return

        val boundingBox = BoundingBox.fromGeoPoints(points)
        mapView.zoomToBoundingBox(boundingBox, true, 100)
    }

    // 🧭 Navigate to nearest colony
    private fun navigateToNearestColony() {

        val myLocation = locationOverlay.myLocation ?: return

        var nearestMarker: Marker? = null
        var minDistance = Double.MAX_VALUE

        for (marker in colonyMarkers) {
            val distance =
                myLocation.distanceToAsDouble(marker.position)

            if (distance < minDistance) {
                minDistance = distance
                nearestMarker = marker
            }
        }

        nearestMarker?.let {
            mapView.controller.animateTo(it.position)
            mapView.controller.setZoom(17.5)
            it.showInfoWindow()
        }
    }

    // 📍 Center on user
    private fun centerOnUser() {
        locationOverlay.myLocation?.let {
            mapView.controller.animateTo(it)
            mapView.controller.setZoom(16.5)
        }
    }
    private fun getScaledMarker(resId: Int, sizeDp: Int): android.graphics.drawable.Drawable {
        val density = resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()

        val drawable = requireContext().getDrawable(resId)!!

        val bitmap = android.graphics.Bitmap.createBitmap(
            sizePx,
            sizePx,
            android.graphics.Bitmap.Config.ARGB_8888
        )

        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return android.graphics.drawable.BitmapDrawable(resources, bitmap)
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
