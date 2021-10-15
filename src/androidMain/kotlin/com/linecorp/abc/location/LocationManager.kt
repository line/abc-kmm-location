package com.linecorp.abc.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

internal actual class LocationManager {

    // -------------------------------------------------------------------------------------------
    //  Public (Actual)
    // -------------------------------------------------------------------------------------------

    actual fun isPermissionAllowed() = focusedActivity?.let {
        ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    } ?: false

    actual fun removeAllListeners() { }

    actual fun removeListeners(target: Any) { }

    actual fun requestPermission() {
        focusedActivity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestPermissionsRequestCode
            )
        }
    }

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdating() {
        if (focusedActivity == null) { return }

        if (!isPermissionAllowed()) {
            requestPermission()
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    actual fun stopLocationUpdating() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // -------------------------------------------------------------------------------------------
    //  Internal
    // -------------------------------------------------------------------------------------------

    @SuppressLint("StaticFieldLeak")
    internal var activity: Activity? = null

    internal fun configure(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location = locationResult?.locations?.last() ?: return
                val coordinates = Coordinates(location.latitude, location.longitude)
                val data = LocationData(
                    location.accuracy.toDouble(),
                    location.altitude,
                    0.0,
                    location.bearing.toDouble(),
                    location.speed.toDouble(),
                    coordinates)
                ABCLocation.notifyOnLocationUpdated(data)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    ABCLocation.notifyOnLocationUnavailable()
                }
            }
        }
        val settings = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        buildLocationRequest()
        LocationServices
            .getSettingsClient(context)
            .checkLocationSettings(settings)
    }

    internal fun processRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestPermissionsRequestCode -> {
                if (grantResults.isEmpty()) {
                    return
                }
                when (grantResults[0]) {
                    PackageManager.PERMISSION_GRANTED -> {
                        startLocationUpdating()
                        ABCLocation.notifyOnPermissionUpdated(true)
                    }
                    PackageManager.PERMISSION_DENIED ->
                        ABCLocation.notifyOnPermissionUpdated(false)
                    else -> Unit
                }
            }
        }
    }

    internal fun showNotificationSetting() {
        val activity = focusedActivity ?: return
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                data = Uri.fromParts("package", activity.applicationInfo.packageName, null)
                activity.startActivity(this)
            }
    }

    // -------------------------------------------------------------------------------------------
    //  Private
    // -------------------------------------------------------------------------------------------

    private val requestPermissionsRequestCode = 4885

    private val focusedActivity: Activity?
        get() = activity?.let {
            if (it.isFinishing) null else { it }
        }

    @SuppressLint("StaticFieldLeak")
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fastestInterval = 1 * 1000
            interval = 1 * 1000
            smallestDisplacement = 10f
        }
    }
}