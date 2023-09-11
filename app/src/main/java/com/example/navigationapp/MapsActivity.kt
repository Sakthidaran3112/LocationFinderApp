package com.example.navigationapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.navigationapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val REQUEST_LOCATION_PERMISSION = 1
    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.setMyLocationEnabled(true)
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()

        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnMapClickListener { latLng ->
            val geocoder = Geocoder(this, Locale.getDefault())

            try {
                val addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )
                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    val addressText = address.getAddressLine(0)

                    selectedMarker?.remove()

                    // Creates a marker at the clicked location
                    val newMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(addressText)
                    )

                    selectedMarker = newMarker

                    newMarker?.showInfoWindow()

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        mMap.setOnInfoWindowClickListener { marker ->
            openContactDialog(marker.title)
        }

    }

    private fun openContactDialog(address: String?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_box, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.address)
        val messageTextView = dialogView.findViewById<TextView>(R.id.address_displayed)
        val directionsButton = dialogView.findViewById<CardView>(R.id.direction_cardview)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_close)

        titleTextView.text = "Address"
        messageTextView.text = address

        val builder = AlertDialog.Builder(this, R.style.PauseDialog)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            builder.dismiss()
        }

        directionsButton.setOnClickListener {
            val intentUri = Uri.parse("google.navigation:q=$address")
            val intent = Intent(Intent.ACTION_VIEW, intentUri)
            intent.setPackage("com.google.android.apps.maps")

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show()
            }
        }

        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
}