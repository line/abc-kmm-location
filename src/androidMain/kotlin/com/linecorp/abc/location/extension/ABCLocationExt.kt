package com.linecorp.abc.location.extension

import android.app.Activity
import android.app.Application
import android.content.Context
import com.linecorp.abc.location.ABCLocation
import com.linecorp.abc.location.SharedLocationRequest
import com.linecorp.abc.location.observers.ActivityLifecycleObserver

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

fun ABCLocation.Companion.setLocationRequest(sharedLocationRequest: SharedLocationRequest) =
    locationManager.setLocationRequest(sharedLocationRequest)


internal var ABCLocation.Companion.activity: Activity?
    get() = locationManager.activity
    set(value) { locationManager.activity = value }

internal fun ABCLocation.Companion.configure(context: Context) {
    locationManager.configure(context)

    val application = context.applicationContext as? Application ?: return
    application.registerActivityLifecycleCallbacks(ActivityLifecycleObserver)
}