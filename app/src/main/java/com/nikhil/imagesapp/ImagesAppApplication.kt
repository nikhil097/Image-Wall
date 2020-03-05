package com.nikhil.imagesapp

import android.content.Context
import com.nikhil.imagesapp.inject.DaggerAppComponent
import com.nikhil.imagesapp.inject.NetworkModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ImagesAppApplication : DaggerApplication() {

    val TAG = ImagesAppApplication::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    companion object {
        lateinit var instance: ImagesAppApplication
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder()
            .application(this)
            .networkModule(NetworkModule(BuildConfig.API_BASE_URL))
            .build()
        appComponent.inject(this)
        return appComponent
    }
}