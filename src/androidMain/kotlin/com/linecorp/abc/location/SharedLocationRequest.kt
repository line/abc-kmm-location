package com.linecorp.abc.location

data class SharedLocationRequest(
    var priority: SharedPriority = SharedPriority.PRIORITY_BALANCED_POWER_ACCURACY,
    var fastestInterval: Long? = null,
    var interval: Long? = null,
    var maxWaitTime: Long? = null,
    var smallestDisplacement: Float? = null,
    var isWaitForAccurateLocation: Boolean? = null,
    var numUpdates: Int? = null,
    var expirationTime: Long? = null,
) {
    companion object {
        enum class SharedPriority(val value: Int) {
            PRIORITY_HIGH_ACCURACY(100),
            PRIORITY_BALANCED_POWER_ACCURACY(102),
            PRIORITY_LOW_POWER(104),
            PRIORITY_NO_POWER(105)
        }
    }
}