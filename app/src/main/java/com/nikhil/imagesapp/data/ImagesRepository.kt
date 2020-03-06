package com.nikhil.imagesapp.data

import com.facebook.internal.ImageResponse
import com.nikhil.imagesapp.data.remote.ApiService
import com.nikhil.imagesapp.models.Image
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ImagesRepository@Inject constructor(private val mApiService: ApiService) {

    fun getAllImages(): Observable<List<Image>> {
        return mApiService.allImages()
            .map { it.data }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}