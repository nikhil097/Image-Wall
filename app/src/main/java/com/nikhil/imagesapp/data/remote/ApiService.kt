package com.nikhil.imagesapp.data.remote

import com.nikhil.imagesapp.models.ImagesResponse
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("images")
    fun allImages(): Observable<ImagesResponse>

    @Multipart
    @POST("images")
    fun uploadImage(@Part image: MultipartBody.Part): Completable
}