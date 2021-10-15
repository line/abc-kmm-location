package com.linecorp.abc.location

import com.linecorp.abc.location.extension.requiredPermission
import kotlinx.cinterop.useContents
import platform.CoreLocation.*
import platform.darwin.NSObject

internal class CLLocationManagerDelegate: NSObject(), CLLocationManagerDelegateProtocol {

    // -------------------------------------------------------------------------------------------
    //  Implementation of CLLocationManagerDelegateProtocol
    // -------------------------------------------------------------------------------------------

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) =
        notifyDidChangeAuthorization(
            LocationAuthorizationStatus.fromInt(manager.authorizationStatus)
        )

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) =
        notifyDidChangeAuthorization(
            LocationAuthorizationStatus.fromInt(didChangeAuthorizationStatus)
        )

    private fun notifyDidChangeAuthorization(status: LocationAuthorizationStatus) {
        val isGranted = ABCLocation.requiredPermission == status ||
                LocationAuthorizationStatus.AuthorizedAlways == status
        ABCLocation.notifyOnPermissionUpdated(isGranted)
        ABCLocation.startLocationUpdating()
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun locationManager(manager: CLLocationManager, didStartMonitoringForRegion: CLRegion) { }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun locationManager(manager: CLLocationManager, didEnterRegion: CLRegion) { }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun locationManager(manager: CLLocationManager, didExitRegion: CLRegion) { }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) =
        notify(didUpdateLocations.last() as? CLLocation, manager.heading)

    override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) =
        notify(manager.location, didUpdateHeading)

    // -------------------------------------------------------------------------------------------
    //  Private
    // -------------------------------------------------------------------------------------------

    private fun notify(lastLocation: CLLocation?, lastHeading: CLHeading?) {
        val location = lastLocation ?: return
        val heading = lastHeading?.trueHeading ?: 0.0
        location.coordinate.useContents {
            val coordinates = Coordinates(
                latitude,
                longitude
            )
            val data = LocationData(
                location.horizontalAccuracy,
                location.altitude,
                location.verticalAccuracy,
                heading,
                location.speed,
                coordinates
            )
            ABCLocation.notifyOnLocationUpdated(data)
        }
    }
}