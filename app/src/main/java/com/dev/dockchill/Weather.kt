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
 *  Irakurtzen du API klabea ___.txt fitxategitik
 *  apikey.txt .gitignore karpetan gordeta dago, kanpotik deskargatu behar izango da
 *  app/src/main/assets/apikey.txt izango da bere ruta
 *  Retrofit instantzia sortu
 *  HTTP eskaera egin
 *  interfazean erabiltzeko datuak bueltatu
 */

class Weather(private val context: Context) {

    // erabiliko da hau lortzeko GPS lokalizazioa, beharko dira erabiltzailearen partetik
    // baimenak ematea GPS erabiltzeko
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Retrofit-en bidez deituko dugu WeatherAPI-ren datuei
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

    // APIren erantzunaren egitura, JSON datuak Kotlin objektu bihurtzeko
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

    // hemen lortuko dugu API deia egitea eta datuar Retrofit bidez json batera bidaltzea datuak
    // parseatzeko.
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create()) // Automatikoki JSON => Kotlin
            .build()
    }

    /**
     * Eguraldiaren datuak lortzen ditu latitude eta longitude-aren arabera.
     * Koroutina batean exekutatzen da (Dispatchers.IO), sareko eskaerak blokeatu ez dezan UI haria.
     * @param lat latitude-a
     * @param lon longitude-a
     * @param onResult emaitza bueltatzen duen callback-a (WeatherResponse edo null)
     */
    fun getWeatherData(lat: Double, lon: Double, onResult: (WeatherResponse?) -> Unit) {
        val apiKey = getapiKey()
        if (apiKey.isEmpty()) {
            Log.e("Weather", "API key not found!")
            onResult(null)
            return
        }

        val retrofit = createRetrofit()
        val service = retrofit.create(WeatherApi::class.java)

        // Eskaera exekutatzen da IO harian, sareko operazioetarako egokia den Dispatcherren barruan
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getForecast(apiKey, "$lat,$lon")

                // Callback-era emaitza bueltatzen da (UI harian kudeatuko da gero)
                onResult(response)

            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    /**
     * FusedLocationProviderClient erabiliz erabiltzailearen kokapena lortzen du.
     * GPS baimenak behar dira (ACCESS_FINE_LOCATION eta ACCESS_COARSE_LOCATION).
     * @param onLocationReceived callback bat kokapena jaso ondoren exekutatzen dena.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getGpsLocation(onLocationReceived: (Location?) -> Unit) {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            onLocationReceived(location) // Kokapena ongi jaso bada, callback-era bidali
        }.addOnFailureListener {
            it.printStackTrace()
            onLocationReceived(null) // Akatsen kasuan, null bueltatu
        }
    }

    /**
     * API deia egiteko URL osoa eraikitzen du, probaren edo log-erako erabilgarria.
     * Ez du eskaera exekutatzen, soilik URL-a prestatzen du.
     */
    fun getapidata(lat: Double, lon: Double) {
        val apikey = getapiKey()
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=$apikey&q=$lat,$lon&days=2&aqi=no&alerts=no"
        //Log.d("aitor", url) ongi funtzionatzen du printzipioz
    }

    /**
     * apikey.txt fitxategitik irakurtzen du API klabea.
     * Fitxategia "assets" karpetan egon behar du (app/src/main/assets/apikey.txt).
     * Akats baten kasuan, kate huts bat bueltatzen du.
     */
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
