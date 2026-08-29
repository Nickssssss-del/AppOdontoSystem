package com.odontosystem.app.data.model

import java.io.Serializable

data class Dentist(
    val id: String,
    val name: String,
    val specialty: String,
    val district: String,
    val address: String,
    val rating: Float,
    val reviewsCount: Int,
    val price: Double,
    val availableDays: List<String>,
    val phone: String = "51987654321", // Número telefónico para enlace directo con la API de WhatsApp
    val imageUrl: String? = null,
    val avatarInitials: String = "DR"
) : Serializable
