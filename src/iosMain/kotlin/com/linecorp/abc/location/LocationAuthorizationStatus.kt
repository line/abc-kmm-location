package com.linecorp.abc.location

enum class LocationAuthorizationStatus(val rawValue: Int) {
    NotSet(-1),
    NotDetermined(0),
    Restricted(1),
    Denied(2),
    AuthorizedAlways(3),
    AuthorizedWhenInUse(4);

    companion object {
        fun fromInt(value: Int) = values().first { it.rawValue == value }
    }
}