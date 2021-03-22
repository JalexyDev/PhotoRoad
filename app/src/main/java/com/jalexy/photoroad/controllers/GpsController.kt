package com.jalexy.photoroad.controllers

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.jalexy.photoroad.activities.CameraActivity.Companion.REQUEST_CHECK_SETTINGS
import com.jalexy.photoroad.activities.MapsActivity

class GpsController(private val activity: AppCompatActivity, private val onTrack: OnTrack) {

    var callbackInterval = 50
    var locationUpdateState = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    var lastLocation: Location? = null

    fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun initController() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                // если мы изъявили желание отслеживать разницу в текущем положении
                if (locationUpdateState) {
                    var enoughDistance = true
                    val currentLocation = p0.lastLocation

                    val hdop = currentLocation.accuracy / 5

                    // точность достаточно велика
                    if (hdop > 1f) {

                        // и пройденное растояние больше указанного интервала
                        val lastL = lastLocation
                        lastL?.let {
                            enoughDistance = lastL.distanceTo(currentLocation) > callbackInterval
                        }
                        //если мы первый раз фоткаем, то можно и так
                    } else enoughDistance = lastLocation == null

                    // обновляем точку отсчета и делаем фотку
                    if (enoughDistance) {
                        lastLocation = currentLocation
                        onTrack.onUpdate()
                    }
                }
            }
        }

        createLocationRequest()
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapsActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        onTrack.onRefresh()

        locationUpdateState = true

        //todo почитать про Looper (который сейчас null)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = REQUEST_INTERVAL
        locationRequest.fastestInterval = FASTEST_REQUEST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            if (locationUpdateState) {
                startLocationUpdates()
            }
        }

        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(
                        activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    interface OnTrack {
        fun onUpdate()
        fun onRefresh()
    }

    companion object {
        private const val REQUEST_INTERVAL = 5000L
        private const val FASTEST_REQUEST_INTERVAL = 2000L
    }
}