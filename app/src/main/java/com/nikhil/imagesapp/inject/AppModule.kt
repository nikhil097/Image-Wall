package com.nikhil.imagesapp.inject

import android.content.Context
import com.nikhil.imagesapp.ImagesAppApplication
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun provideContext(application: ImagesAppApplication): Context {
        return application.applicationContext
    }
}