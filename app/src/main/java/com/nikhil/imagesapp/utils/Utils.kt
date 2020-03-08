package com.nikhil.imagesapp.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nikhil.imagesapp.R
import com.yalantis.ucrop.UCrop
import java.io.File

fun startCropActivity(file: File, context: AppCompatActivity){
    val options = UCrop.Options()
    options.setHideBottomControls(true)
    options.withAspectRatio(1f, 1f)
    options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
    options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))

    UCrop.of(Uri.fromFile(file), Uri.fromFile(File(context.cacheDir, System.currentTimeMillis().toString())))
        .withOptions(options)
        .withMaxResultSize(512, 512)
        .start(context)
}