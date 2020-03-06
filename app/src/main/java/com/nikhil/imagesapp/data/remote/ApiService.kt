package com.nikhil.imagesapp.data.remote

import androidx.collection.ArrayMap
import com.facebook.internal.ImageResponse
import com.nikhil.imagesapp.models.ImagesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApiService {

    @GET("")
    fun allImages(): Observable<ImagesResponse>

}