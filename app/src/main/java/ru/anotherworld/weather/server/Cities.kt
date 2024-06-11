package ru.anotherworld.weather.server

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Cities(
    val id: String,
    val city: String,
    val latitude: String,
    val longitude: String
)
