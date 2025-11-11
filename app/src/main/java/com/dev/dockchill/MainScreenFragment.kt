package com.dev.dockchill

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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

    // Fragment-aren layout-aren binding objektua
    private lateinit var binding: FragmentMainScreenBinding

    // Eguraldiaren datuak kudeatzeko klasearen instantzia
    private lateinit var weather: Weather

    // GPS baimena eskatzeko erabiliko den kodea
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Fragment-aren layout-a inflatu eta binding sortu
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Fragment-a berriz bistaratzean ordua eta data eguneratu
        showDateTime()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Weather klasearen instantzia sortu, eguraldiaren datuak lortzeko
        weather = Weather(requireContext())

        // ordua eta data kargatzeko / erakusteko
        showDateTime()

        // GPSaren kokapena lortzen saiatuko gara
        getLocation()
    }

    // data eta hordua lortuko dugu hemendik eta binding bidez ezarri fragmentuan
    private fun showDateTime() {
        val now = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.timeText.text = timeFormat.format(now)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateText.text = dateFormat.format(now)
    }

    /**
     * kokapena lortzeko metodoa.
     * Lehenik eta behin egiaztatzen du ea erabiltzaileak GPS baimena eman duen.
     * Baimena badago -> GPS kokapena lortzen du eta handleLocation() metodoari pasatzen dio.
     * Baimenik ez badago -> sistemari baimena eskatu.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            // baimenik badu... -> lortutako kokapena kudeatu, adibidez api deitu
            // handlelocation() barruan
            weather.getGpsLocation { location ->
                handleLocation(location)
            }

        } else {
            // bestela eskatu baimenak
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * GPS kokapena jaso ondoren exekutatzen den metodoa.
     * Latitude eta longitude balioekin WeatherAPI deitzen du eta emaitza UI-n erakusten du.
     */
    private fun handleLocation(location: Location?) {
        if (location != null) {
            val lat = location.latitude
            val lon = location.longitude

            // API deia egin eguraldiaren datuak jasotzeko
            weather.getWeatherData(lat, lon) { result ->
                if (result != null) {
                    // Eguraldiaren kodea eta egoera lortu
                    val code = result.current.condition.code
                    val biharcode = result.forecast.forecastday[1].day.condition.code
                    val info = weatherMap[code]

                    // GAUR: une honetako tenperatura eta egoera
                    val temp = result.current.temp_c
                    val condition = result.current.condition.text

                    // Honek egiten duena da denbora errealean datuak eguneratu
                    activity?.runOnUiThread {
                        binding.weatherCard.gaurTemperatureText.text = "$temp°C"
                        binding.weatherCard.gaurWeatherDescription.text =
                            info?.descriptionEus ?: condition
                        info?.let { binding.weatherCard.gaurWeatherIcon.setImageResource(it.iconRes) }
                    }

                    // BIHAR: biharko iragarpena
                    val forecastTomorrow = result.forecast.forecastday[1].day
                    val tempTomorrow = forecastTomorrow.avgtemp_c
                    val conditionTomorrow = forecastTomorrow.condition.text

                    activity?.runOnUiThread {
                        binding.weatherCard.biharTemperatureText.text = "$tempTomorrow°C"
                        binding.weatherCard.biharWeatherDescription.text =
                            info?.descriptionEus ?: conditionTomorrow
                        info?.let { binding.weatherCard.biharWeatherIcon.setImageResource(it.iconRes) }
                    }

                } else {
                    // Ezin izan da API erantzunik jaso (null), errorea log-ean eta UI-n erakutsi
                    Log.e("aitor", "WeatherResponse nulo {lat}, {lon}")
                    activity?.runOnUiThread {
                        binding.weatherCard.gaurTemperatureText.text = "Error °C"
                        binding.weatherCard.gaurWeatherDescription.text = "Error"
                        binding.weatherCard.biharTemperatureText.text = "Error °C"
                        binding.weatherCard.biharWeatherDescription.text = "Error"
                    }
                }
            }

        } else {
            // Ez da GPS kokapena lortu (null) -> mezu bat erakutsi eta UI-n errorea jarri
            Toast.makeText(requireContext(), "Ezin izan da lortu GPS", Toast.LENGTH_SHORT).show()
            binding.weatherCard.gaurTemperatureText.text = "Error °C"
            binding.weatherCard.gaurWeatherDescription.text = "Error"
            binding.weatherCard.biharTemperatureText.text = "Error °C"
            binding.weatherCard.biharWeatherDescription.text = "Error"
        }
    }
}
