package com.linecorp.abc.location.utils

data class Version(val value: String) : Comparable<Version> {
    private val split by lazy {
        value.split("-")
            .first()
            .split(".")
            .map { it.toIntOrNull() ?: 0 }
    }

    override fun compareTo(other: Version): Int {
        for (i in 0 until maxOf(split.size, other.split.size)) {
            val compare = split.getOrElse(i) { 0 }.compareTo(other.split.getOrElse(i) { 0 })
            if (compare != 0)
                return compare
        }
        return 0
    }
}