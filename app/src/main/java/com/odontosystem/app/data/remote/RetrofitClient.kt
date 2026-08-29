package com.odontosystem.app.data.remote

import com.odontosystem.app.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://odontosystem-api.onrender.com/" // Endpoint de producción/Render

    private lateinit var apiServiceInstance: ApiService

    fun init(sessionManager: SessionManager) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(MockApiInterceptor()) // Permite fallback a datos demostrativos
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiServiceInstance = retrofit.create(ApiService::class.java)
    }

    val apiService: ApiService
        get() {
            check(::apiServiceInstance.isInitialized) {
                "RetrofitClient debe ser inicializado en la clase Application con RetrofitClient.init(sessionManager)"
            }
            return apiServiceInstance
        }
}
