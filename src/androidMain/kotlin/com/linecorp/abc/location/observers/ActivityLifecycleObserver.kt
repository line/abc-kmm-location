package com.linecorp.abc.location.observers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.linecorp.abc.location.ABCLocation
import com.linecorp.abc.location.extension.activity

internal object ActivityLifecycleObserver : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStarted(p0: Activity) {}
    override fun onActivityDestroyed(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        val currentActivity = ABCLocation.activity
        if (currentActivity != null && !currentActivity.isFinishing) {
            return
        }
        ABCLocation.activity = p0
    }

    override fun onActivityResumed(p0: Activity) {}
}