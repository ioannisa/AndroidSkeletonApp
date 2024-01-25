package com.example.democompose

import android.app.Application
import com.example.democompose.manager.CredentialManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class DemoApplication: Application() {

    @Inject lateinit var credentialManager: CredentialManager

    override fun onCreate() {
        super.onCreate()
        credentialManager.setApiKey(BuildConfig.API_KEY)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            initSoLoader()
        }

        Timber.d("THE API KEY IS ${credentialManager.getApiKey()}")
    }
}