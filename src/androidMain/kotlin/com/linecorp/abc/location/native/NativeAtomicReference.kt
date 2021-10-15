package com.linecorp.abc.location.native

import java.util.concurrent.atomic.AtomicReference

internal actual class NativeAtomicReference<T> actual constructor(value: T) {

    private val atomic = AtomicReference(value)

    actual var value: T
        get() = atomic.get()
        set(value) = atomic.set(value)
}