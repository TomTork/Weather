package ru.anotherworld.weather.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/*
Класс для получения данных из запросов, используется библиотека Ktor,
данные представляются с помощью data-классов.
В функции getWeather игнорируются все поля, кроме main, откуда и берётся значение температуры
 */

class MyClient {
    private val citiesUrl = "https://gist.githubusercontent.com/Stronger197/764f9886a1e8392ddcae2521437d5a3b/raw/65164ea1af958c75c81a7f0221bead610590448e/cities.json"
    suspend fun getCities(): List<Cities>?{
        return try {
            val client = HttpClient(CIO)
            val response = client.get(citiesUrl)
            return Json.decodeFromString<List<Cities>>(response.body())
        } catch (e: Exception){
            null
        }
    }
    private val apiKey = "02b1c094039028a8320f00e3e56b00ba"
    suspend fun getWeather(latitude: String, longitude: String): WeatherMain?{
        return try {
            val json = Json{ ignoreUnknownKeys = true }
            val client = HttpClient(CIO){
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                    })
                }
            }
            val response = client.get("https://api.openweathermap.org/data/2.5/weather?" +
                    "lat=$latitude&lon=$longitude&exclude=minutely%2Chourly%2Cdaily%2Calerts&units" +
                    "=metric&appid=$apiKey")
            val obj = json.parseToJsonElement(response.body()) as JsonObject
            return json.decodeFromString<WeatherMain>(obj["main"].toString())
        } catch (e: Exception){
            null
        }
    }
}