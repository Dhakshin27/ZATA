package com.zata.zata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.zata.zata.model.ColonyReport
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var tvTotal: TextView
    private lateinit var tvConfirmed: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvLast: TextView
    private lateinit var progressConfidence: ProgressBar
    private lateinit var tvRate: TextView
    private lateinit var tvHighest: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Cards
        val cardTotal = view.findViewById<View>(R.id.card_total)
        val cardConfirmed = view.findViewById<View>(R.id.card_confirmed)
        val cardRate = view.findViewById<View>(R.id.card_rate)
        val cardHighest = view.findViewById<View>(R.id.card_highest)

        // Text inside cards
        tvTotal = cardTotal.findViewById(R.id.stat_value)
        tvConfirmed = cardConfirmed.findViewById(R.id.stat_value)
        tvRate = cardRate.findViewById(R.id.stat_value)
        tvHighest = cardHighest.findViewById(R.id.stat_value)

        // Labels
        cardTotal.findViewById<TextView>(R.id.stat_label).text = "TOTAL REPORTS"
        cardConfirmed.findViewById<TextView>(R.id.stat_label).text = "CONFIRMED"
        cardRate.findViewById<TextView>(R.id.stat_label).text = "CONFIRM RATE"
        cardHighest.findViewById<TextView>(R.id.stat_label).text = "HIGHEST CONF."

        // Other views
        tvAvg = view.findViewById(R.id.tv_avg_confidence)
        tvLast = view.findViewById(R.id.tv_last_report)
        progressConfidence = view.findViewById(R.id.progress_confidence)

        loadDashboardData()
        return view
    }


    private fun loadDashboardData() {

        FirebaseFirestore.getInstance()
            .collection("colonies")
            .addSnapshotListener { snapshots, error ->

                if (snapshots == null || error != null) return@addSnapshotListener

                val reports = snapshots.toObjects(ColonyReport::class.java)

                val total = reports.size
                val confirmed = reports.count { it.rockBee }


                val confidences = reports
                    .filter { it.rockBee }

                    .map { it.confidence * 100 }

                val avgConfidence = confidences.average().toInt()
                val highestConfidence = confidences.maxOrNull()?.toInt() ?: 0

                val confirmationRate =
                    if (total > 0) (confirmed * 100 / total) else 0

                val lastTimestamp =
                    reports.maxOfOrNull { it.timestamp }

                // UI
                tvTotal.text = total.toString()
                tvConfirmed.text = confirmed.toString()
                tvAvg.text = "$avgConfidence%"
                progressConfidence.progress = avgConfidence

                tvRate.text = "$confirmationRate%"
                tvHighest.text = "$highestConfidence%"

                tvLast.text =
                    lastTimestamp?.let {
                        "Last report: ${formatDate(it)}"
                    } ?: "No recent activity"
            }
    }


    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
