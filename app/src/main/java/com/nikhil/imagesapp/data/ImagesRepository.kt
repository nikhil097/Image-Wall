package com.nikhil.imagesapp.data

import com.nikhil.imagesapp.data.remote.ApiService
import com.nikhil.imagesapp.models.Image
import com.nikhil.imagesapp.models.ImageUploadResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class ImagesRepository@Inject constructor(private val mApiService: ApiService) {

    fun getAllImages(): Observable<List<Image>> {
        return mApiService.allImages()
            .map { it.data }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun uploadImage(selectedFile: File): Observable<String> {
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), selectedFile)
        val body = MultipartBody.Part.createFormData("photo", selectedFile.name, requestFile)

        return mApiService.uploadImage(body)
            .map { it.message ?: "Uploaded" }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
    }

}