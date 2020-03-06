package com.nikhil.imagesapp.ui.home.imageSourceOptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.ui.base.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.bottom_sheet_choose_image_source.*

class ChooseImageSourceBottomSheet : BaseBottomSheetFragment() {

    companion object {
        private val TAG = ChooseImageSourceBottomSheet::class.java.simpleName

        fun showDialog(callbacks: Callbacks, fragmentManager: androidx.fragment.app.FragmentManager) {
            val dialog = ChooseImageSourceBottomSheet()
            dialog.setCallbacks(callbacks)
            dialog.show(fragmentManager, "[CHOOSE_IMAGE_SOURCE_BOTTOM_SHEET]")
        }
    }

    interface Callbacks {
        fun onCameraSelected()
        fun onGallerySelected()
    }

    private var mCallbacks: Callbacks? = null

    fun setCallbacks(callbacks: Callbacks) {
        mCallbacks = callbacks
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_choose_image_source, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image_gallery.setOnClickListener {
            mCallbacks?.onGallerySelected()
            dismiss()
        }

        image_camera.setOnClickListener {
            mCallbacks?.onCameraSelected()
            dismiss()
        }
    }
}