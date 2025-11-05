package com.dev.dockchill

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


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

    fun getapidata(lat: Double, lon: Double) { }
}


