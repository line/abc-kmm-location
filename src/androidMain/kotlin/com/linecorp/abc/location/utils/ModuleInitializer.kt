package com.linecorp.abc.location.utils

import android.content.Context
import androidx.startup.Initializer
import com.linecorp.abc.location.ABCLocation
import com.linecorp.abc.location.extension.configure


@Suppress("UNUSED")
internal class ModuleInitializer: Initializer<Int> {
    override fun create(context: Context): Int {
        ABCLocation.configure(context)
        return 0
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}