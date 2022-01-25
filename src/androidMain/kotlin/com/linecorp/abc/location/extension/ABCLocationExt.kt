package com.linecorp.abc.location.extension

import android.app.Activity
import android.content.Context
import android.location.Location
import com.linecorp.abc.location.ABCLocation
import com.linecorp.abc.location.ABCLocationRequest
import com.linecorp.abc.location.Coordinates
import com.linecorp.abc.location.LocationData
import java.lang.ref.WeakReference

fun ABCLocation.Companion.processRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String?>,
    grantResults: IntArray
) = locationManager.processRequestPermissionsResult(
    requestCode,
    permissions,
    grantResults
)

fun ABCLocation.Companion.showNotificationSetting() =
    locationManager.showNotificationSetting()

fun ABCLocation.Companion.setLocationRequest(locationRequest: ABCLocationRequest) =
    locationManager.setLocationRequest(locationRequest)


internal var ABCLocation.Companion.activity: Activity?
    get() = locationManager.activity?.get()
    set(value) { locationManager.activity = WeakReference(value) }

internal fun ABCLocation.Companion.configure(context: Context) =
    locationManager.configure(context)

fun Location.toLocationData(): LocationData = LocationData(
    accuracy.toDouble(),
    altitude,
    0.0,
    bearing.toDouble(),
    speed.toDouble(),
    Coordinates(latitude, longitude)
)