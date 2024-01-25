package com.example.democompose

import android.app.Application

/**
 * the SoLoader is used in debug build to load flipper for network debugging
 * and utilizes a package from facebok which is available only in debug builds
 */
fun Application.initSoLoader() {
    // No implementation for release - only for debug
}