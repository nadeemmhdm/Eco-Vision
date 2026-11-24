package org.ecovision.app

import android.app.Application

class EcoVisionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: EcoVisionApplication
            private set
    }
}
