package com.nikhil.imagesapp.widgets

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nikhil.imagesapp.R

open class RoundedCornerBottomSheet: BottomSheetDialogFragment() {
    override fun onStart() {
        super.onStart()

        view?.post {
            val parent = view?.parent as View
            parent.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet)
        }

    }
}