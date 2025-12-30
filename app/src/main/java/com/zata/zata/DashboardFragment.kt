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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvTotal = view.findViewById(R.id.tv_total)
        tvConfirmed = view.findViewById(R.id.tv_confirmed)
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

                val reports = snapshots.documents.mapNotNull {
                    it.toObject(ColonyReport::class.java)
                }

                val total = reports.size
                val confirmed = reports.count { it.isRockBee }

                val avgConfidence =
                    reports
                        .filter { it.isRockBee }
                        .map { it.confidence }
                        .average()
                        .takeIf { !it.isNaN() }
                        ?.times(100)
                        ?.toInt() ?: 0

                val lastTimestamp =
                    reports.maxOfOrNull { it.timestamp }

                // UI updates
                tvTotal.text = total.toString()
                tvConfirmed.text = confirmed.toString()
                tvAvg.text = "$avgConfidence%"
                progressConfidence.progress = avgConfidence

                tvLast.text =
                    if (lastTimestamp != null)
                        "Last Reported: ${formatDate(lastTimestamp)}"
                    else
                        "No reports yet"
            }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
