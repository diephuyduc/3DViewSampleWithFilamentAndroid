package com.example.a3dmodelsample

import android.app.Application
import com.google.android.filament.utils.Utils

class MApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init()
    }
}