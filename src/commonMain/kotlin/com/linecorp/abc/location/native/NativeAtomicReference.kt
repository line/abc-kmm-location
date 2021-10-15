package com.linecorp.abc.location.native

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect class NativeAtomicReference<T>(value: T) {
    var value: T
}