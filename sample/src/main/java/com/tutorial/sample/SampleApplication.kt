package com.tutorial.sample

import android.app.Application
import android.util.Log

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Since Timber is in the library module, we'll use standard Android logging for the sample app
        Log.d("SampleApplication", "Application created")
    }
}