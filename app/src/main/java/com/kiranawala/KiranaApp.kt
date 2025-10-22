package com.kiranawala

import android.app.Application
import com.kiranawala.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class KiranaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        Timber.d("Kiranawala App initialized successfully")
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}