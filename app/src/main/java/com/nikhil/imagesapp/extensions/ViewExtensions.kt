package com.nikhil.imagesapp.extensions

import android.content.Context
import android.view.View
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
