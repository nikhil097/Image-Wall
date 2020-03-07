package com.nikhil.imagesapp.inject

import com.nikhil.imagesapp.ui.camera.CameraActivity
import com.nikhil.imagesapp.ui.home.HomeActivity
import com.nikhil.imagesapp.ui.home.imageSourceOptions.ChooseImageSourceBottomSheet
import com.nikhil.imagesapp.ui.viewImage.ViewImageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun homeActivity(): HomeActivity

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun chooseImageSourceBottomSheet(): ChooseImageSourceBottomSheet

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun cameraActivity(): CameraActivity

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun viewImageActivity(): ViewImageActivity

}