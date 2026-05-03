package com.example.safespotfinalapps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

class MyLocation : AppCompatActivity() {

    private lateinit var ivBack: ImageButton
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private lateinit var tvAddress: TextView
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var selectedGeoPoint: GeoPoint? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_mylocation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack = findViewById(R.id.ivBack)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.btnCancel)
        tvAddress = findViewById(R.id.tv_address)
        mapView = findViewById(R.id.mapView)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)

        val defaultPoint = GeoPoint(12.8797, 121.7740)
        mapView.controller.setCenter(defaultPoint)

        mapView.overlays.add(object : org.osmdroid.views.overlay.Overlay() {
            override fun onSingleTapConfirmed(
                e: android.view.MotionEvent,
                mapView: MapView
            ): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                setSelectedLocation(geoPoint)
                return true
            }
        })

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        ivBack.setOnClickListener { finish() }

        btnCancel.setOnClickListener {
            Toast.makeText(this, "Location selection cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnConfirm.setOnClickListener {
            val address = tvAddress.text.toString()
            if (address == "Locating..." || address.isEmpty()) {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show()
            } else {
                val resultIntent = Intent()
                resultIntent.putExtra("selected_address", address)
                resultIntent.putExtra("selected_lat", selectedGeoPoint?.latitude ?: 0.0)
                resultIntent.putExtra("selected_lng", selectedGeoPoint?.longitude ?: 0.0)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun enableMyLocation() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                val myLocation = myLocationOverlay.myLocation
                if (myLocation != null) {
                    mapView.controller.setCenter(myLocation)
                    setSelectedLocation(myLocation)
                }
            }
        }
        mapView.overlays.add(myLocationOverlay)
    }

    private fun setSelectedLocation(geoPoint: GeoPoint) {
        selectedGeoPoint = geoPoint

        mapView.overlays.removeAll(mapView.overlays.filterIsInstance<Marker>())

        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Location"
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint)
        mapView.invalidate()

        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    if (!address.thoroughfare.isNullOrEmpty()) append(address.thoroughfare + ", ")
                    if (!address.locality.isNullOrEmpty()) append(address.locality + ", ")
                    if (!address.adminArea.isNullOrEmpty()) append(address.adminArea)
                }
                tvAddress.text = addressText.ifEmpty { "Location: ${geoPoint.latitude}, ${geoPoint.longitude}" }
            } else {
                tvAddress.text = "Location: ${geoPoint.latitude}, ${geoPoint.longitude}"
            }
        } catch (e: Exception) {
            tvAddress.text = "Location: ${geoPoint.latitude}, ${geoPoint.longitude}"
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