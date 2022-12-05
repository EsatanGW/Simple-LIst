package com.esatan.base.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    vararg callFactories: CallAdapter.Factory = emptyArray()
) {

    companion object {
        private const val DOMAIN = "reqres.in"
        private const val HOST = "https://$DOMAIN/api/"
    }

    private val retrofit: Retrofit

    init {
        val gson: Gson = GsonBuilder().setLenient().create()

        val okHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val builder = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(HOST)
            .apply {
                if (callFactories.isNotEmpty()) {
                    callFactories.forEach { addCallAdapterFactory(it) }
                }
            }

        retrofit = builder.build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)
}