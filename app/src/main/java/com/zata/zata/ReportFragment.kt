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
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_report, container, false)

        imgPreview = view.findViewById(R.id.img_preview)
        btnCapture = view.findViewById(R.id.btn_capture)
        btnSubmit = view.findViewById(R.id.btn_submit)

        btnCapture.setOnClickListener { openCamera() }

        btnSubmit.setOnClickListener { uploadReport() }
        txtStatus = view.findViewById(R.id.txt_status)
        progress = view.findViewById(R.id.progress_analysis)


        return view
    }

    // 📍 Get accurate location
    private fun getLocation() {
        val locationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
    }

    // 📸 Open camera
    private fun openCamera() {
        cameraLauncher.launch(null)
    }


    private val cameraLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            if (bitmap != null) {
                imageBitmap = bitmap
                imgPreview.setImageBitmap(bitmap)
                txtStatus.text = "Image ready for analysis"
                btnSubmit.isEnabled = true
            }
        }


    // ☁️ Upload image + save Firestore
    private fun uploadReport() {
        if (imageBitmap == null) return

        progress.visibility = View.VISIBLE
        btnSubmit.isEnabled = false
        btnCapture.isEnabled = false
        txtStatus.text = "Getting location..."

        val locationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            resetUIOnFailure("Location permission not granted")
            return
        }

        locationClient.lastLocation.addOnSuccessListener { location ->

            if (location == null) {
                resetUIOnFailure("Unable to get location. Try again.")
                return@addOnSuccessListener
            }

            latitude = location.latitude
            longitude = location.longitude

            txtStatus.text = "Analyzing image..."

            analyzeRockBee(imageBitmap!!) { isRockBee, confidence ->

                progress.visibility = View.GONE
                btnCapture.isEnabled = true
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

                saveToFirestore(isRockBee, confidence)
            }
        }
    }



    // Helper to keep the code clean and re-enable UI on errors
    private fun resetUIOnFailure(message: String) {
        progress.visibility = View.GONE
        btnSubmit.isEnabled = true
        btnCapture.isEnabled = true
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }



    private fun saveToFirestore(
        isRockBee: Boolean,
        confidence: Float
    ) {
        Log.d("REPORT",
            "Saving colony at $latitude , $longitude")

        val report = ColonyReport(
            lat = latitude,
            lng = longitude,
            isRockBee = isRockBee,
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
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Failed to save colony", e)
                Toast.makeText(
                    context,
                    "Firestore error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

    }



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

                var isRockBee = false
                var confidence = 0f

                for (label in labels) {
                    val text = label.text.lowercase()

                    if (
                        text.contains("bee") ||
                        text.contains("insect") ||
                        text.contains("wasp")
                    ) {
                        isRockBee = true
                        confidence = label.confidence
                        break
                    }
                }

                callback(isRockBee, confidence)
            }
            .addOnFailureListener {
                callback(false, 0f)
            }
    }

}
