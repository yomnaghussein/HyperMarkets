package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.MainActivityBinding
import com.example.myapplication.ui.viewmodel.MainActivityViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: MainActivityViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private var autoCompleteFragment: AutocompleteSupportFragment? = null
    private var listOfMarkers: MutableList<Marker> = arrayListOf()
    private var lastMarkerClicked: Marker? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showNotificationNotAllowedAlert()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MainActivityBinding.inflate(layoutInflater).root)
        requestNotificationPermission()
        initMaps()
        initAutoCompleteSearch()
        observeOnAutoCompleteListeners()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
                && shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                showNotificationAlert()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showNotificationAlert() {
        AlertDialog.Builder(this)
            .setTitle(R.string.allow_notification)
            .setMessage(R.string.allow_notification_message)
            .setPositiveButton(
                R.string.ok
            ) { _, _ -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            .setNegativeButton(R.string.not_now) { _, _ -> }
            .show()
    }

    private fun showNotificationNotAllowedAlert() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notification_not_allowed)
            .setMessage(R.string.notification_not_allowed_desc)
            .setPositiveButton(
                R.string.ok
            ) { _, _ -> }.show()
    }

    private fun initMaps() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fcMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initAutoCompleteSearch() {
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        autoCompleteFragment = supportFragmentManager.findFragmentById(R.id.fcAutoCompleteFragment)
                as AutocompleteSupportFragment

        autoCompleteFragment?.setPlaceFields(
            listOf(
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.NAME
            )
        )
        autoCompleteFragment?.setLocationBias(
            RectangularBounds.newInstance(
                LatLng(24.645187, 46.301553),
                LatLng(25.154851, 47.250297)
            )
        )
    }

    private fun observeOnAutoCompleteListeners() {
        autoCompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_finding_place), Toast.LENGTH_LONG
                ).show()
            }

            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    addMarkerToMap(it, place.name)
                    zoomOnMap(it)
                }
            }
        })
    }

    private fun zoomOnMap(latLng: LatLng) {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng, 30f)
        googleMap?.animateCamera(newLatLngZoom)
    }

    private fun addMarkerToMap(latLng: LatLng, name: String? = null): Marker? {
        val marker = googleMap?.addMarker(
            MarkerOptions().position(latLng)
                .title(name ?: latLng.toString())
                .snippet(getString(R.string.tap_again_to_remove))
        )
        marker?.tag = latLng.toString()
        return marker
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap?.let {
            it.mapType = GoogleMap.MAP_TYPE_NORMAL
            it.uiSettings.isZoomGesturesEnabled = true
            it.uiSettings.isZoomControlsEnabled = true
        }

        setMapToRiyadhBounds()
        getTopHyperMarkets()
        observeOnTopHyperMarkets()
        observeOnMapsListeners()

    }

    private fun setMapToRiyadhBounds() {
        val riyadhBounds = LatLngBounds(
            LatLng(24.556319, 46.509352),
            LatLng(25.087293, 46.836196)
        )

        val newLatLngZoom = CameraUpdateFactory.newLatLngBounds(riyadhBounds, 40)
        googleMap?.animateCamera(newLatLngZoom)
    }

    private fun getTopHyperMarkets() {
        viewModel.getPlaces()
    }

    private fun observeOnTopHyperMarkets() {
        viewModel.placesLiveData.observe(this) { listOfHyperMarkets ->
            listOfHyperMarkets.forEach {
                addMarkerToMap(it.latLng, it.name)
            }
        }
    }

    private fun observeOnMapsListeners() {
        googleMap?.let {
            it.setOnMapLongClickListener { latLng ->
                val searchedMarker = listOfMarkers.find { it.tag == latLng.toString() }
                if (searchedMarker == null) {
                    val marker = addMarkerToMap(latLng)
                    marker?.let { it1 -> listOfMarkers.add(it1) }
                }
            }
            it.setOnMarkerClickListener { marker ->
                if (marker == lastMarkerClicked) {
                    val addedMarker = listOfMarkers.find { it == marker }
                    marker.remove()
                    addedMarker?.let { listOfMarkers.remove(marker) }
                } else {
                    lastMarkerClicked = marker
                }
                false
            }
        }
    }

}

