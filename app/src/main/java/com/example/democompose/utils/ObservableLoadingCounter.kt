package com.example.democompose.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

class ObservableLoadingInteger(initialValue: Int) {
    private val _value = MutableStateFlow(initialValue)
    val value: StateFlow<Int> = _value.asStateFlow()

    private val atomicInteger = AtomicInteger(initialValue)

    fun incrementAndGet(): Int {
        val newValue = atomicInteger.incrementAndGet()
        _value.value = newValue
        return newValue
    }

    fun decrementAndGet(): Int {
        val newValue = atomicInteger.decrementAndGet()
        _value.value = newValue
        return newValue
    }

    fun updateAndGet(newValue: Int) {
        atomicInteger.updateAndGet { newValue }
        _value.value = newValue
    }

    fun get(): Int {
        return atomicInteger.get()
    }
}
