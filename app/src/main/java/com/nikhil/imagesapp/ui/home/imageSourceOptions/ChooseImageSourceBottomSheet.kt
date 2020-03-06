package com.nikhil.imagesapp.ui.home.imageSourceOptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.ui.base.BaseBottomSheetFragment

class ChooseImageSourceBottomSheet : BaseBottomSheetFragment() {

    companion object {
        private val TAG = ChooseImageSourceBottomSheet::class.java.simpleName

        fun showDialog(fragmentManager: androidx.fragment.app.FragmentManager) {
            val dialog = ChooseImageSourceBottomSheet()
            dialog.show(fragmentManager, "[CHOOSE_IMAGE_SOURCE_BOTTOM_SHEET]")
        }
    }

    interface Callbacks {
        fun onCameraSelected()
        fun onGallerySelected()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_choose_image_source, container, false)
    }


}