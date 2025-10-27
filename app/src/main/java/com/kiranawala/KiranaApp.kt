package com.kiranawala

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.kiranawala.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class KiranaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializePlaces()
        initializeLogging()
        Timber.d("Kiranawala App initialized successfully")
    }

    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}