package com.planscollective.plansapp.webServices.base

import com.planscollective.plansapp.constants.Urls
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WebServiceBuilder {
    private var client : OkHttpClient
    private var retrofitPlans : Retrofit
    private var builderRetrofit: Retrofit.Builder

    init {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        clientBuilder.interceptors().add(loggingInterceptor)
        client = clientBuilder.build()

        builderRetrofit = Retrofit.Builder()
            .baseUrl(Urls.BASE_URL_BACKEND)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())

        retrofitPlans = builderRetrofit.build()
    }

    fun<T> buildService(service: Class<T>, baseUrl: String? = null) : T {
        return if (baseUrl.isNullOrEmpty()) {
            retrofitPlans.create(service)
        }else {
            builderRetrofit.baseUrl(baseUrl).build().create(service)
        }
    }

}