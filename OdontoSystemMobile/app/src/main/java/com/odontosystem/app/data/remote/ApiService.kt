package com.odontosystem.app.data.remote

import com.odontosystem.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("api/v1/dentists")
    suspend fun getDentists(
        @Query("district") district: String? = null,
        @Query("query") query: String? = null
    ): Response<List<Dentist>>

    @GET("api/v1/appointments/my-appointments")
    suspend fun getAppointments(): Response<List<Appointment>>

    @POST("api/v1/appointments")
    suspend fun createAppointment(
        @Body request: CreateAppointmentRequest
    ): Response<Appointment>

    @POST("api/v1/chatbot/message")
    suspend fun sendChatMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
