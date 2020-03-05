package com.nikhil.imagesapp.inject

import android.app.Application
import com.nikhil.imagesapp.ImagesAppApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
    AndroidSupportInjectionModule::class,
    AppModule::class,
    NetworkModule::class,
    ActivityBindingModule::class,
    ViewModelModule::class))
interface AppComponent : AndroidInjector<ImagesAppApplication> {

    override fun inject(instance: ImagesAppApplication)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): AppComponent.Builder

        fun build(): AppComponent

        fun networkModule(networkModule: NetworkModule): Builder
    }
}