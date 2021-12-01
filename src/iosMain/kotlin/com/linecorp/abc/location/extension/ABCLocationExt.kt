package com.linecorp.abc.location.extension

import com.linecorp.abc.location.ABCLocation
import com.linecorp.abc.location.ABCLocationCompanion
import com.linecorp.abc.location.LocationAuthorizationStatus

typealias OnAlwaysAllowsPermissionRequiredBlock = () -> Unit

var ABCLocation.Companion.requiredPermission: LocationAuthorizationStatus
    get() = locationManager.requiredPermission
    set(value) { locationManager.requiredPermission = value }

fun ABCLocation.Companion.onAlwaysAllowsPermissionRequired(
    target: Any,
    block: OnAlwaysAllowsPermissionRequiredBlock
): ABCLocationCompanion {
    locationManager.onAlwaysAllowsPermissionRequired(target, block)
    return this
}

fun ABCLocation.Companion.removeOnAlwaysAllowsPermissionRequired(target: Any) =
    locationManager.removeOnAlwaysAllowsPermissionRequired(target)
    
internal var ABCLocation.Companion.previousAuthorizationStatus: LocationAuthorizationStatus
    get() = locationManager.previousAuthorizationStatus.value
    set(value) { locationManager.previousAuthorizationStatus.value = value }
