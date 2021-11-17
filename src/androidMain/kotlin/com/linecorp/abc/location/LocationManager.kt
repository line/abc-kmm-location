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
import com.linecorp.abc.location.ABCLocation.Companion.notifyOnLocationUnavailable
import com.linecorp.abc.location.utils.LocationUtil

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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestPermissionsRequestCode
            )
        }
    }

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdating() {
        val activity = focusedActivity ?: return

        if (!isPermissionAllowed()) {
            requestPermission()
            notifyOnLocationUnavailable()
        } else if(!LocationUtil.checkLocationEnable(activity)) {
            notifyOnLocationUnavailable()
        } else {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

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
            // 일부 Device 에서 onLocationAvailability 의 값을 믿을 수 없어 사용하지 않음
        }

        val settings = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        LocationServices
            .getSettingsClient(context)
            .checkLocationSettings(settings)
    }

    internal fun processRequestPermissionsResult(
        requestCode: Int,
        @Suppress("UNUSED_PARAMETER")
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
    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fastestInterval = 1 * 1000L
        interval = 10 * 1000L
    }

    fun setLocationRequest(sharedLocationRequest: SharedLocationRequest) {
        locationRequest = LocationRequest.create().apply {
            priority = sharedLocationRequest.priority.value
            sharedLocationRequest.fastestInterval?.let { fastestInterval = it }
            sharedLocationRequest.interval?.let { interval = it }
            sharedLocationRequest.maxWaitTime?.let { maxWaitTime = it }
            sharedLocationRequest.smallestDisplacement?.let { smallestDisplacement = it }
            sharedLocationRequest.isWaitForAccurateLocation?.let { isWaitForAccurateLocation = it }
            sharedLocationRequest.numUpdates?.let { numUpdates = it }
            sharedLocationRequest.expirationTime?.let { expirationTime = it }
        }
    }
}