package ru.training.pikabu

import android.app.Application

class PikabuApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PikabuApplication
            private set
    }
}