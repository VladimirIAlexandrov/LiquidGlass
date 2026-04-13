package com.liquidglass.blur3

internal class Blur3HashImpl {
    private var hash = 0L
    private var unsupported = false

    fun start() { hash = 0; unsupported = false }
    fun get() = if (unsupported) -1L else hash
    fun isUnsupported() = unsupported
    fun add(value: Long) { hash = hash * 31 + value }
    fun unsupported() { unsupported = true }

    companion object {
        fun calcHash(hash: Long, value: Long) = hash * 31 + value
    }
}
