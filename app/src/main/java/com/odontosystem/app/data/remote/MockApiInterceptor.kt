package com.odontosystem.app.data.remote

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject

class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // If trying to connect to a real backend and it fails, or for testing demo mode:
        return try {
            val response = chain.proceed(request)
            if (response.code != 404 && response.code != 502 && response.code != 503) {
                return response
            }
            generateMockResponse(request, path)
        } catch (e: Exception) {
            generateMockResponse(request, path)
        }
    }

    private fun generateMockResponse(request: Request, path: String): Response {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        var responseString = ""
        var code = 200

        when {
            path.contains("auth/login") -> {
                val json = JSONObject()
                json.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock_jwt_token_odontosystem")
                val userJson = JSONObject()
                userJson.put("id", "usr_1001")
                userJson.put("name", "Nicole De La Cruz")
                userJson.put("email", "nicole@odontosystem.com")
                userJson.put("role", "PATIENT")
                json.put("user", userJson)
                responseString = json.toString()
            }

            path.contains("dentists") -> {
                val array = JSONArray()

                val d1 = JSONObject().apply {
                    put("id", "dnt_1")
                    put("name", "Dr. Roberto Carlos Mendoza")
                    put("specialty", "Ortodoncia y Estética Dental")
                    put("district", "Miraflores")
                    put("address", "Av. Larco 1020, Of. 402")
                    put("rating", 4.9)
                    put("reviewsCount", 128)
                    put("price", 150.0)
                    put("availableDays", JSONArray().apply { put("Lunes"); put("Miércoles"); put("Viernes") })
                }

                val d2 = JSONObject().apply {
                    put("id", "dnt_2")
                    put("name", "Dra. Lucía María Fernández")
                    put("specialty", "Endodoncia y Odontopediatría")
                    put("district", "San Isidro")
                    put("address", "Av. Javier Prado Este 2450")
                    put("rating", 4.8)
                    put("reviewsCount", 94)
                    put("price", 180.0)
                    put("availableDays", JSONArray().apply { put("Martes"); put("Jueves"); put("Sábado") })
                }

                val d3 = JSONObject().apply {
                    put("id", "dnt_3")
                    put("name", "Dr. Alejandro Ruiz")
                    put("specialty", "Cirugía Maxilofacial e Implantes")
                    put("district", "Surco")
                    put("address", "Av. Encalada 650")
                    put("rating", 4.95)
                    put("reviewsCount", 210)
                    put("price", 220.0)
                    put("availableDays", JSONArray().apply { put("Lunes"); put("Martes"); put("Jueves") })
                }

                val d4 = JSONObject().apply {
                    put("id", "dnt_4")
                    put("name", "Dra. Patricia Valenzuela")
                    put("specialty", "Odontología General y Limpieza")
                    put("district", "San Borja")
                    put("address", "Av. Aviación 3120")
                    put("rating", 4.7)
                    put("reviewsCount", 85)
                    put("price", 120.0)
                    put("availableDays", JSONArray().apply { put("Lunes"); put("Viernes"); put("Sábado") })
                }

                array.put(d1)
                array.put(d2)
                array.put(d3)
                array.put(d4)
                responseString = array.toString()
            }

            path.contains("appointments") && request.method == "GET" -> {
                val array = JSONArray()
                val a1 = JSONObject().apply {
                    put("id", "apt_5001")
                    put("dentistId", "dnt_1")
                    put("dentistName", "Dr. Roberto Carlos Mendoza")
                    put("specialty", "Ortodoncia")
                    put("district", "Miraflores")
                    put("date", "2026-09-02")
                    put("time", "10:30 AM")
                    put("reason", "Revisión de brackets y limpieza")
                    put("status", "Confirmada")
                }
                array.put(a1)
                responseString = array.toString()
            }

            path.contains("appointments") && request.method == "POST" -> {
                val json = JSONObject().apply {
                    put("id", "apt_" + System.currentTimeMillis())
                    put("dentistId", "dnt_1")
                    put("dentistName", "Dr. Especialista Seleccionado")
                    put("specialty", "Odontología General")
                    put("district", "Miraflores")
                    put("date", "2026-09-05")
                    put("time", "11:00 AM")
                    put("reason", "Consulta Odontológica")
                    put("status", "Confirmada")
                }
                responseString = json.toString()
            }

            path.contains("chatbot/message") -> {
                val json = JSONObject()
                json.put("reply", "¡Hola! He procesado tu solicitud. Puedes agendar tu cita seleccionando un odontólogo por distrito en la pantalla principal o indicándome la especialidad que buscas.")
                responseString = json.toString()
            }

            else -> {
                code = 404
                responseString = "{\"error\": \"Not Found\"}"
            }
        }

        return Response.Builder()
            .code(code)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(responseString.toResponseBody(jsonMediaType))
            .addHeader("content-type", "application/json")
            .build()
    }
}
