package com.nikhil.imagesapp.data.remote

class DataWrapper<T>(var response: T? = null, var error: Throwable? = null, var isLoading: Boolean = false) {

}