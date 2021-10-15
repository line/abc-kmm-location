package com.linecorp.abc.location.extension

fun <E> List<E>.appending(element: E): List<E> {
    val mutableList = toMutableList()
    mutableList.add(element)
    return mutableList.toList()
}

fun <E> List<E>.removed(element: E): List<E> {
    val mutableList = toMutableList()
    mutableList.remove(element)
    return mutableList.toList()
}