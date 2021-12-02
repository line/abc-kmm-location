package com.linecorp.abc.location

import com.linecorp.abc.location.extension.OnAlwaysAllowsPermissionRequiredBlock
import com.linecorp.abc.location.extension.appending
import com.linecorp.abc.location.extension.removed
import com.linecorp.abc.location.native.NativeAtomicReference
import com.linecorp.abc.location.utils.Version
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLLocationAccuracyBestForNavigation
import platform.UIKit.UIDevice

internal actual class LocationManager {

    // -------------------------------------------------------------------------------------------
    //  Public (Actual)
    // -------------------------------------------------------------------------------------------

    actual fun isPermissionAllowed() =
        authorizationStatus == requiredPermission.value

    actual fun removeAllListeners() {
        onAlwaysAllowsPermissionRequiredBlockMap.value = emptyMap()
    }

    actual fun removeListeners(target: Any) =
        removeOnAlwaysAllowsPermissionRequired(target)

    actual fun requestPermission() = if (isRequiredAllowAlways) {
        locationManager.requestAlwaysAuthorization()
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
    } else {
        locationManager.requestWhenInUseAuthorization()
    }

    actual fun startLocationUpdating() = when (authorizationStatus) {
        LocationAuthorizationStatus.AuthorizedAlways -> startUpdating()
        LocationAuthorizationStatus.AuthorizedWhenInUse -> if (isRequiredAllowAlways) {
            if (previousAuthorizationStatus.value == LocationAuthorizationStatus.AuthorizedWhenInUse) {
                notifyOnAlwaysAllowsPermissionRequired()
            } else {
                requestPermission()
            }
        } else {
            startUpdating()
        }
        LocationAuthorizationStatus.Denied -> ABCLocation.notifyOnLocationUnavailable()
        else -> requestPermission()
    }

    actual fun stopLocationUpdating() {
        if (!locationManager.locationServicesEnabled) { return }
        locationManager.stopUpdatingHeading()
        locationManager.stopUpdatingLocation()
    }

    // -------------------------------------------------------------------------------------------
    //  Public
    // -------------------------------------------------------------------------------------------

    var requiredPermission = NativeAtomicReference(LocationAuthorizationStatus.AuthorizedAlways)
    val previousAuthorizationStatus = NativeAtomicReference(LocationAuthorizationStatus.NotSet)

    fun onAlwaysAllowsPermissionRequired(
        target: Any,
        block: OnAlwaysAllowsPermissionRequiredBlock
    ) {
        onAlwaysAllowsPermissionRequiredBlockMap.value =
            onAlwaysAllowsPermissionRequiredBlockMap.value.appending(target, block)
    }

    fun removeOnAlwaysAllowsPermissionRequired(target: Any) {
        onAlwaysAllowsPermissionRequiredBlockMap.value =
            onAlwaysAllowsPermissionRequiredBlockMap.value.removed(target)
    }

    // -------------------------------------------------------------------------------------------
    //  Private
    // -------------------------------------------------------------------------------------------

    private val isRequiredAllowAlways: Boolean
        get() = requiredPermission.value == LocationAuthorizationStatus.AuthorizedAlways

    private val authorizationStatus: LocationAuthorizationStatus
        get() = if (Version(UIDevice.currentDevice.systemVersion) >= Version("14")) {
            LocationAuthorizationStatus.fromInt(CLLocationManager().authorizationStatus)
        } else {
            LocationAuthorizationStatus.fromInt(CLLocationManager.authorizationStatus())
        }

    private val locationManager: CLLocationManager by lazy {
        val manager = CLLocationManager()
        manager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        manager.setDelegate(locationDelegate)
        manager
    }

    private val locationDelegate: CLLocationManagerDelegate by lazy {
        CLLocationManagerDelegate()
    }

    private val onAlwaysAllowsPermissionRequiredBlockMap = NativeAtomicReference(mapOf<Any, OnAlwaysAllowsPermissionRequiredBlock>())

    private fun notifyOnAlwaysAllowsPermissionRequired() {
        onAlwaysAllowsPermissionRequiredBlockMap.value.forEach { it.value() }
    }

    private fun startUpdating() {
        if (!locationManager.locationServicesEnabled) { return }
        locationManager.startUpdatingHeading()
        locationManager.startUpdatingLocation()
    }
}