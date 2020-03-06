package com.nikhil.imagesapp.extensions

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.nikhil.imagesapp.ImagesAppApplication

fun getSnackBar(view: View, value: String): Snackbar {
    return getSnackBar(view, value, Snackbar.LENGTH_LONG)
}

fun getSnackBar(view: View, value: String, length: Int): Snackbar {
    return Snackbar.make(view, value, length)
}

fun showSnackBar(view: View, value: String) {
    getSnackBar(view, value).show()
}

fun showSnackBar(view: View, value: Int) {
    getSnackBar(view, view.context.getString(value)).show()
}

fun showSnackBar(view: View, value: Int, length: Int) {
    getSnackBar(view, view.context.getString(value), length).show()
}

fun getString(context: Context?, stringRes: Int) : String {
    return context!!.resources.getString(stringRes)
}

fun getString(stringRes: Int) : String {
    return ImagesAppApplication.instance.resources.getString(stringRes)
}

inline fun <T:Any, R> whenNotNull(input: T?, callback: (T)->R): R? {
    return input?.let(callback)
}

fun ImageView.loadImageUrl(imageUrl: String, requestOptions: RequestOptions = RequestOptions.centerCropTransform(), roundedCorners: Boolean = false) {
    val options = if(roundedCorners) {
        RequestOptions().transform(CenterCrop(), RoundedCorners(8))
    } else requestOptions

    Glide.with(this)
        .load(imageUrl)
        .apply(options.priority(Priority.HIGH))
        .into(this)
}