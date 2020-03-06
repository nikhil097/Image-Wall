package com.nikhil.imagesapp.models

import com.google.gson.annotations.SerializedName

class ImagesResponse (
@SerializedName("data")
val data: List<Image>,
@SerializedName("code")
val code: Int,
@SerializedName("success")
val success: Boolean
)

class Image (
    @SerializedName("url")
    val imageUrl : String
)