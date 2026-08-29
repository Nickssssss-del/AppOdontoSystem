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
    val availableSlots: List<String>? = emptyList(),
    val phone: String = "51987654321",
    val imageUrl: String? = null,
    val avatarInitials: String = "DR"
) : Serializable
