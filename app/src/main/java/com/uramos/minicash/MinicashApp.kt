package com.uramos.minicash

import android.app.Application
import com.uramos.minicash.ui.utils.AppStrings

class MinicashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
    }
}
