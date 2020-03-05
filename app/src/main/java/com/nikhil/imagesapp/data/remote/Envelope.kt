package com.nikhil.imagesapp.data.remote

import androidx.annotation.NonNull
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

data class Envelope<T>(
        val code: String,
        val success: Boolean,
        val data: T
)

@Suppress("UNCHECKED_CAST")
class UnwrapConverterFactory(private val factory: GsonConverterFactory) : Converter.Factory() {

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> {
        // e.g. WrappedResponse<Person>
        val wrappedType = object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> {
                // -> WrappedResponse<type>
                return arrayOf(type)
            }

            override fun getOwnerType(): Type? {
                return null
            }

            override fun getRawType(): Type {
                return Envelope::class.java
            }
        }
        val gsonConverter = factory.responseBodyConverter(wrappedType, annotations, retrofit)
        return WrappedResponseBodyConverter(gsonConverter as Converter<ResponseBody, Envelope<`$Gson$Types`>>)
    }
}

class WrappedResponseBodyConverter<T>(private val converter: Converter<ResponseBody, Envelope<T>>) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(@NonNull value: ResponseBody): T {
        val response = converter.convert(value)
        return response.data
        /*// RxJava will call onError with this exception
        throw new WrappedError(response.getResultCode());*/
    }
}