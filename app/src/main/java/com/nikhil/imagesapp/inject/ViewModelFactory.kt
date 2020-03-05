package com.nikhil.imagesapp.inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ViewModelFactory : ViewModelProvider.Factory {

    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>

    @Inject
    constructor(viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>) {
        this.viewModels = viewModels
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModels[modelClass]?.get() as T
}
