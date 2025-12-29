package com.zata.zata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.zata.zata.model.ColonyReport
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var tvTotal: TextView
    private lateinit var tvConfirmed: TextView
    private lateinit var tvAvgConfidence: TextView
    private lateinit var tvLastReport: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvTotal = view.findViewById(R.id.tv_total_colonies)
        tvConfirmed = view.findViewById(R.id.tv_confirmed_colonies)
        tvAvgConfidence = view.findViewById(R.id.tv_avg_confidence)
        tvLastReport = view.findViewById(R.id.tv_last_report)

        loadDashboardData()

        return view
    }

    private fun loadDashboardData() {

        FirebaseFirestore.getInstance()
            .collection("colonies")
            .get()
            .addOnSuccessListener { documents ->

                var total = 0
                var confirmed = 0
                var confidenceSum = 0f
                var lastTimestamp: Long? = null

                for (doc in documents) {
                    val report = doc.toObject(ColonyReport::class.java)
                    total++

                    confidenceSum += report.confidence

                    if (report.isRockBee && report.confidence >= 0.6f) {
                        confirmed++
                    }

                    val ts = doc.getTimestamp("timestamp")?.toDate()?.time
                    if (ts != null && (lastTimestamp == null || ts > lastTimestamp!!)) {
                        lastTimestamp = ts
                    }
                }

                tvTotal.text = "Total Colonies: $total"
                tvConfirmed.text = "Confirmed Rock Bee Colonies: $confirmed"

                val avg = if (total > 0) (confidenceSum / total) * 100 else 0f
                tvAvgConfidence.text =
                    "Average Confidence: ${avg.toInt()}%"

                tvLastReport.text = lastTimestamp?.let {
                    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    "Last Reported: ${sdf.format(Date(it))}"
                } ?: "Last Reported: --"
            }
    }
}
