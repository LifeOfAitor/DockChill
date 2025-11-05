package com.dev.dockchill

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dev.dockchill.databinding.FragmentMainScreenBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainScreenFragment : Fragment() {

    private lateinit var binding: FragmentMainScreenBinding
    private lateinit var weather: Weather
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weather = Weather(requireContext())

        // Mostrar fecha y hora
        showDateTime()

        // Intentar obtener ubicación
        getLocation()
    }

    private fun showDateTime() {
        val now = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.timeText.text = timeFormat.format(now)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateText.text = dateFormat.format(now)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            // baimenik badu...
            weather.getGpsLocation { location ->
                handleLocation(location)
            }

        } else {
            // bestela eskatu baimenak
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun handleLocation(location: Location?) {
        if (location != null) {
            val lat = location.latitude
            val lon = location.longitude
            Toast.makeText(requireContext(), "Lat: $lat, Lon: $lon", Toast.LENGTH_SHORT).show()

            weather.getapidata(lat, lon)
        } else {
            Toast.makeText(requireContext(), "Ezin izan da lortu GPS", Toast.LENGTH_SHORT).show()
        }
    }
}
