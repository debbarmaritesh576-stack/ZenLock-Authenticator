package com.aegis.pdf

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import com.aegis.pdf.features.organizer.data.manager.TagManager
import javax.inject.Inject

@HiltAndroidApp
class AegisPdfApp : Application() {

    @Inject
    lateinit var tagManager: TagManager

    override fun onCreate() {
        super.onCreate()
        
        Log.d("AegisPdfApp", "Application initialized")
        
        initializeTags()
    }

    private fun initializeTags() {
        Thread {
            try {
                tagManager.initDefaultTags()
                Log.d("AegisPdfApp", "Default tags initialized")
            } catch (e: Exception) {
                Log.e("AegisPdfApp", "Failed to initialize tags", e)
            }
        }.start()
    }
}