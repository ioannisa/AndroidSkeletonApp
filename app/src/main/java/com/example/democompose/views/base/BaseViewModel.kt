package com.example.democompose.views.base

import androidx.annotation.OpenForTesting
import androidx.lifecycle.ViewModel
import com.example.democompose.utils.ObservableLoadingInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

abstract class BaseViewModel constructor() : ViewModel() {

    @Inject protected lateinit var loadingCounter: ObservableLoadingInteger
    // Method to manually inject ObservableLoadingInteger for testing

    @OpenForTesting
    fun testInjects(loadingCounter: ObservableLoadingInteger) {
        this.loadingCounter = loadingCounter
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val mutex = Mutex()

    fun setLoading(boolean: Boolean) {
        when (boolean) {
            true -> if (loadingCounter.incrementAndGet() >= 1) {
                _isLoading.value = true
            }
            false -> if (loadingCounter.decrementAndGet() == 0) {
                _isLoading.value = false
            }
        }
    }

    // Scoped function for loading
    suspend inline fun loading(block: () -> Unit) = mutex.withLock {
        setLoading(true)
        try {
            block.invoke()
        } finally {
            setLoading(false)
        }
    }


    private val _isLoadingPull = MutableStateFlow(false)
    val isLoadingPull = _isLoadingPull.asStateFlow()

    fun setLoadingPull(boolean: Boolean) {
        _isLoadingPull.value = boolean

        // adjust the counter to block rotation through main activity
        if (boolean) {
            loadingCounter.incrementAndGet()
        } else {
            loadingCounter.decrementAndGet()
        }
    }

    // Scoped function for pull to refresh loading
    suspend inline fun loadingPull(block: () -> Unit) = mutex.withLock {
        setLoadingPull(true)
        try {
            block.invoke()
        } finally {
            setLoadingPull(false)
        }
    }
}