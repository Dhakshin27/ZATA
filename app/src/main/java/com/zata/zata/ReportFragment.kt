package com.zata.zata

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zata.zata.model.ColonyReport
import java.io.ByteArrayOutputStream
import java.util.UUID
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions


class ReportFragment : Fragment() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnCapture: Button
    private lateinit var btnSubmit: Button
    private lateinit var txtStatus: TextView
    private lateinit var progress: ProgressBar

    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_report, container, false)

        imgPreview = view.findViewById(R.id.img_preview)
        btnCapture = view.findViewById(R.id.btn_capture)
        btnSubmit = view.findViewById(R.id.btn_submit)
        txtStatus = view.findViewById(R.id.txt_status)
        progress = view.findViewById(R.id.progress_analysis)

        btnCapture.setOnClickListener { openCamera() }
        btnSubmit.setOnClickListener { startReportFlow() }

        btnSubmit.isEnabled = false
        return view
    }

    // 📸 Camera
    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private val cameraLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            bitmap?.let {
                imageBitmap = it
                imgPreview.setImageBitmap(it)
                txtStatus.text = "Image captured"
                btnSubmit.isEnabled = true
            }
        }

    // 🚀 MAIN FLOW
    private fun startReportFlow() {
        if (imageBitmap == null) return

        progress.visibility = View.VISIBLE
        btnSubmit.isEnabled = false
        txtStatus.text = "Getting location..."

        val locationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showError("Location permission required")
            return
        }

        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                showError("Unable to get location")
                return@addOnSuccessListener
            }

            val lat = location.latitude
            val lng = location.longitude

            txtStatus.text = "Analyzing image..."

            analyzeRockBee(imageBitmap!!) { isRockBee, confidence ->
                progress.visibility = View.GONE
                btnSubmit.isEnabled = true

                if (!isRockBee) {
                    txtStatus.text = "Not a Rock Bee colony"
                    txtStatus.setTextColor(
                        requireContext().getColor(R.color.alert_red)
                    )
                    return@analyzeRockBee
                }

                txtStatus.text =
                    "Rock Bee confirmed (${(confidence * 100).toInt()}%)"
                txtStatus.setTextColor(
                    requireContext().getColor(R.color.alert_green)
                )

                saveColony(lat, lng, confidence)
            }
        }
    }

    // ☁️ Save ONLY LOCATION + ML DATA
    private fun saveColony(lat: Double, lng: Double, confidence: Float) {

        val report = ColonyReport(
            lat = lat,
            lng = lng,
            isRockBee = true,
            confidence = confidence,
            timestamp = System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("colonies")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Colony marked on map",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                showError("Failed to save data")
            }
    }

    private fun showError(msg: String) {
        progress.visibility = View.GONE
        btnSubmit.isEnabled = true
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    // 🧠 ML ANALYSIS
    private fun analyzeRockBee(
        bitmap: Bitmap,
        callback: (Boolean, Float) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build()
        )

        labeler.process(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text.lowercase()
                    if (text.contains("bee") || text.contains("insect")) {
                        callback(true, label.confidence)
                        return@addOnSuccessListener
                    }
                }
                callback(false, 0f)
            }
            .addOnFailureListener {
                callback(false, 0f)
            }
    }
}

