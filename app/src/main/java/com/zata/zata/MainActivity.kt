package com.zata.zata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabReport: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        requestLocationPermission()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)
        fabReport = findViewById(R.id.fab_report)

        // Load default fragment only once
        if (savedInstanceState == null) {
            replaceFragment(MapFragment())
        }

        // Bottom navigation item click handling
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> replaceFragment(MapFragment())
                R.id.nav_alerts -> replaceFragment(AlertsFragment())
                R.id.nav_dashboard -> replaceFragment(DashboardFragment())
                R.id.nav_awareness -> replaceFragment(AwarenessFragment())
            }
            true
        }

        // FAB click → Report screen
        fabReport.setOnClickListener {
            replaceFragment(ReportFragment())
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
