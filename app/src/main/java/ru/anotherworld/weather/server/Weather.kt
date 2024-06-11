package ru.anotherworld.weather.server

import kotlinx.serialization.Serializable

@Serializable
data class WeatherMain(
    val temp: Float
)
