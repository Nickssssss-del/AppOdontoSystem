package com.odontosystem.app.data.model

import java.io.Serializable

enum class AvailabilityFrequency {
    DAILY, WEEKLY
}

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
    val avatarInitials: String = "DR",
    val occupancyLevel: Int = 0, // RF12: 0-100%
    val autoConfirm: Boolean = true, // RF11
    val frequency: AvailabilityFrequency = AvailabilityFrequency.WEEKLY, // RF06
    val verificationStatus: String = "Aprobado" // RF09: Aprobado, Observado, Rechazado
) : Serializable
