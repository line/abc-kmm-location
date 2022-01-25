package com.linecorp.abc.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.linecorp.abc.location.ABCLocation.Companion.notifyOnLocationUnavailable
import com.linecorp.abc.location.extension.toLocationData
import com.linecorp.abc.location.observers.ActivityLifecycleObserver
import com.linecorp.abc.location.utils.LocationUtil
import java.lang.ref.WeakReference


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
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestPermissionsRequestCode
        )
    }

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdating() {
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

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

    //https://developer.android.com/training/location/retrieve-current?hl=ko#BestEstimate
    @SuppressLint("MissingPermission")
    actual fun getCurrentLocation() {
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

        var isLocationNotified = false

        if (!isPermissionAllowed()) {
            requestPermission()
            notifyOnLocationUnavailable()
        } else if(!LocationUtil.checkLocationEnable(activity)) {
            notifyOnLocationUnavailable()
        } else {

            val cts = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { locationResult ->
                isLocationNotified = true
                ABCLocation.notifyOnLocationUpdated(locationResult.toLocationData())

                // For update latest location
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }.addOnFailureListener {}

            Handler(Looper.getMainLooper()).postDelayed({
                if(!isLocationNotified) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { locationResult ->
                        isLocationNotified = true
                        ABCLocation.notifyOnLocationUpdated(locationResult.toLocationData())

                        // For update latest location
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                    }.addOnFailureListener {}
                }
            }, 5 * 1000)

            Handler(Looper.getMainLooper()).postDelayed({
                if(!isLocationNotified) {
                    notifyOnLocationUnavailable()
                }
            }, 10 * 1000)

        }

    }

    // -------------------------------------------------------------------------------------------
    //  Internal
    // -------------------------------------------------------------------------------------------

    internal var activity: WeakReference<Activity>? = null

    internal fun configure(context: Context) {
        val application = context.applicationContext as? Application
        application?.registerActivityLifecycleCallbacks(ActivityLifecycleObserver) ?: run {
            val activity = context.applicationContext as? Activity
            this.activity = WeakReference(activity)
        }

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
        val activity = focusedActivity ?: run {
            notifyOnLocationUnavailable()
            return
        }

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
        get() = activity?.get()?.let {
            if (it.isFinishing || it.isDestroyed) null else { it }
        }

    @SuppressLint("StaticFieldLeak")
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fastestInterval = 1 * 1000L
        interval = 10 * 1000L
    }

    fun setLocationRequest(abcLocationRequest: ABCLocationRequest) {
        locationRequest = LocationRequest.create().apply {
            priority = abcLocationRequest.priority.value
            abcLocationRequest.fastestInterval?.let { fastestInterval = it }
            abcLocationRequest.interval?.let { interval = it }
            abcLocationRequest.maxWaitTime?.let { maxWaitTime = it }
            abcLocationRequest.smallestDisplacement?.let { smallestDisplacement = it }
            abcLocationRequest.isWaitForAccurateLocation?.let { isWaitForAccurateLocation = it }
            abcLocationRequest.numUpdates?.let { numUpdates = it }
            abcLocationRequest.expirationTime?.let { expirationTime = it }
        }
    }
}