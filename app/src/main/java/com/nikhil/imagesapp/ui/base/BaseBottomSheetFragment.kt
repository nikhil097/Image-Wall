package com.nikhil.imagesapp.ui.base

import android.content.Context
import com.nikhil.imagesapp.widgets.RoundedCornerBottomSheet
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

open class BaseBottomSheetFragment : RoundedCornerBottomSheet(), HasAndroidInjector, BaseErrorInterface {

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Any>

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return childFragmentInjector
    }
}
