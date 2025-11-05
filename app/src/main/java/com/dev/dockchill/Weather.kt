package com.dev.dockchill

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException


/**
 * Klase nagusia non eguraldia lortzen duen WeatherAPIren APIa erabiliz
 *  Irakurtzen du API klabea .txt fitxategitik
 *  Retrofit instantzia sortu
 *  HTTP eskaera egin
 *  interfazean erabiltzeko datuak bueltatu
 */

class Weather(private val context: Context) {

    // erabiliko da hau lortzeko GPS lokalizazioa, beharko dira erabiltzailearen partetik
    // baimenak ematea GPS erabiltzeko
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    interface WeatherApi {
        @GET("v1/forecast.json")
        suspend fun getForecast(
            @Query("key") key: String,
            @Query("q") query: String,
            @Query("days") days: Int = 2,
            @Query("aqi") aqi: String = "no",
            @Query("alerts") alerts: String = "no"
        ): WeatherResponse
    }
    data class WeatherResponse(
        val current: CurrentData,
        val forecast: ForecastData
    )
    data class CurrentData(
        val temp_c: Double,
        val condition: ConditionData
    )
    data class ConditionData(
        val text: String,
        val code: Int
    )
    data class ForecastData(
        val forecastday: List<ForecastDayData>
    )

    data class ForecastDayData(
        val day: DayData
    )

    data class DayData(
        val avgtemp_c: Double,
        val condition: ConditionData
    )

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create()) // Cautomatikoki JSON => Kotlin
            .build()
    }

    fun getWeatherData(lat: Double, lon: Double, onResult: (WeatherResponse?) -> Unit) {
        val apiKey = getapiKey()
        if (apiKey.isEmpty()) {
            Log.e("Weather", "API key not found!")
            onResult(null)
            return
        }

        val retrofit = createRetrofit()
        val service = retrofit.create(WeatherApi::class.java)

        // Ejecutamos la petición en un hilo de entrada/salida (IO)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getForecast(apiKey, "$lat,$lon")

                // Llamamos al callback con el resultado
                onResult(response)

            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getGpsLocation(onLocationReceived: (Location?) -> Unit) {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            onLocationReceived(location)
        }.addOnFailureListener {
            it.printStackTrace()
            onLocationReceived(null)
        }
    }

    fun getapidata(lat: Double, lon: Double) {
        val apikey=getapiKey()
        val url = "https://api.weatherapi.com/v1/forecast.json?key=$apikey&q=$lat,$lon&days=2&aqi=no&alerts=no"
        //Log.d("aitor", url) ongi funtzionatzen du printzipioz
    }
    fun getapiKey(): String {
        return try {
            val inputStream = context.assets.open("apikey.txt")
            inputStream.bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}


