package com.example.democompose

import android.app.Application
import com.facebook.soloader.SoLoader

/**
 * the SoLoader is used in debug build to load flipper for network debugging
 * and utilizes a package from facebok which is available only in debug builds
 */
fun Application.initSoLoader() {
    SoLoader.init(this, false)
}