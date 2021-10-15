package com.linecorp.abc.location.extension

fun <K, V> Map<K, V>.appending(key: K, value: V): Map<K, V> {
    val mutableMap = toMutableMap()
    mutableMap[key] = value
    return mutableMap.toMap()
}

fun <K, V> Map<K, V>.removed(key: K): Map<K, V> {
    val mutableMap = toMutableMap()
    mutableMap.remove(key)
    return mutableMap.toMap()
}