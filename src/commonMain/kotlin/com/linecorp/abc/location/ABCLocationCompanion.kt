package com.linecorp.abc.location

typealias OnLocationUpdatedBlock = (location: LocationData) -> Unit
typealias OnLocationUnavailableBlock = () -> Unit
typealias OnPermissionUpdatedBlock = (isGranted: Boolean) -> Unit

interface ABCLocationCompanion {
    fun isPermissionAllowed(): Boolean
    fun currentLocation(block: OnLocationUpdatedBlock)
    fun requestPermission()
    fun startLocationUpdating()
    fun stopLocationUpdating()

    fun onLocationUnavailable(target: Any, block: OnLocationUnavailableBlock): ABCLocationCompanion
    fun onLocationUpdated(target: Any, block: OnLocationUpdatedBlock): ABCLocationCompanion
    fun onPermissionUpdated(target: Any, block: OnPermissionUpdatedBlock): ABCLocationCompanion

    fun removeAllListeners()
    fun removeListeners(target: Any)
    fun removeOnLocationUnavailable(target: Any)
    fun removeOnLocationUpdated(target: Any)
    fun removeOnPermissionUpdated(target: Any)
}