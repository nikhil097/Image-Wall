package com.nikhil.imagesapp.ui.base

import android.view.View
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.data.remote.ErrorResponse
import com.nikhil.imagesapp.data.remote.ErrorUtils
import com.nikhil.imagesapp.extensions.getString
import com.nikhil.imagesapp.extensions.showSnackBar
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

interface BaseErrorInterface {

    fun showError(throwable: Throwable, view: View) {
        when (throwable) {
            is HttpException -> {
                val response = throwable.response()
                val errorResponse = ErrorUtils.parseError(response.errorBody()!!.string(), ErrorResponse::class.java)
                showOtherError(errorResponse, view)
            }
            is SocketTimeoutException -> showSocketTimeoutError(view)
            is IOException -> showInternetError(view)
        }
    }

    fun showInternetError(view: View) {
        showSnackBar(view, getString(view.context!!, R.string.error_msg_no_internet))
    }

    fun showSocketTimeoutError(view: View) {
        showSnackBar(view, getString(view.context!!, R.string.error_msg_timeout))
    }

    fun showOtherError(errorResponse: ErrorResponse, view: View) {
        if (!errorResponse.message.isNullOrEmpty())
            showSnackBar(view, errorResponse.message.toString())
    }
}