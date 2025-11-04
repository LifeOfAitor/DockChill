package com.dev.dockchill

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationSettingsRequest


class Weather {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private fun getapidata() {

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getgpslocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
            }
    }
}

