package com.dev.dockchill

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.util.Log


class Weather(private val context: Context) {

    // erabiliko da hau lortzeko GPS lokalizazioa, beharko dira erabilytzailearen partetik
    // baimenak ematea GPS erabiltzeko
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

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
        Log.d("aitor", url)
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


