package com.example.democompose.di

import android.content.Context
import com.example.democompose.BuildConfig
import com.example.democompose.manager.CredentialManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun NetworkModule.provideOkHttpClientByBuildVariant(appContext: Context, credentialManager: CredentialManager): OkHttpClient {
    val builder = OkHttpClient.Builder()

    if (BuildConfig.DEBUG) {
        builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }

    // optional interceptor to add fixed parameters at every call
    val interceptor = Interceptor { chain ->
        val request = chain.request()
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val apiKey = credentialManager.getApiKey()

        // Logging for debugging
        Timber.d("OkHttp", "Adding parameters: date=$currentDate, apiKey=$apiKey")

        val newUrl = request.url.newBuilder()
            .addQueryParameter("from", currentDate)
            .addQueryParameter("apiKey", apiKey)
            .build()

        val newRequest = request.newBuilder().url(newUrl).build()
        chain.proceed(newRequest)
    }
    builder.addInterceptor(interceptor)

    builder.connectTimeout(30, TimeUnit.SECONDS)
    builder.readTimeout(30, TimeUnit.SECONDS)


    return builder.build()
}
