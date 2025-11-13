package com.dev.dockchill

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dev.dockchill.databinding.FragmentMainScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.widget.SeekBar
import androidx.annotation.RequiresApi
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

    // --- Device Music Controller ---
    private lateinit var mediaSessionManager: MediaSessionManager
    private var mediaController: MediaController? = null
    private var seekBarJob: kotlinx.coroutines.Job? = null

    private fun setupSeekBar() {
        binding.musicCard.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = mediaController?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0
                    val newPosition = (duration * progress / 100)
                    mediaController?.transportControls?.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun startSeekBarUpdater() {
        seekBarJob?.cancel()
        seekBarJob = lifecycleScope.launch {
            while (isActive) {
                val controller = mediaController
                val state = controller?.playbackState
                val metadata = controller?.metadata
                if (controller != null && state != null && metadata != null) {
                    val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
                    val position = state.position
                    val progress = if (duration > 0) (position * 100 / duration).toInt() else 0

                    activity?.runOnUiThread {
                        binding.musicCard.seekBar.progress = progress
                    }
                }
                delay(500)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Fragment-aren layout-a inflatu eta binding sortu
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onResume() {
        super.onResume()

        // Coroutine bat abiarazten dugu fragment bizi den bitartean (lifecycleScope)
        lifecycleScope.launch {
            var azkenEguraldiEguneratzea = 0L // Azken eguraldi eguneratzearen denbora (timestamp)

            while (isActive) {
                val orain = System.currentTimeMillis()

                // 5 segunduro exekutatzen da, beraz ordua eta data fresko mantentzen dira
                showDateTime()

                // Energia aurrezteko ez dugu gehiegi exekutatu nahi GPS eta API deia
                if (orain - azkenEguraldiEguneratzea > 300000) { // 300.000 ms = 5 minutu
                    getLocation()
                    azkenEguraldiEguneratzea = orain
                }

                // 5 segunduro berriro bueltatu buklora
                delay(5000)
            }
        }
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

        // --- Initialize Device Music Controller ---
        initMediaController()
        setupSeekBar()
        startSeekBarUpdater()


        binding.musicCard.btnPlaypause.setOnClickListener {
            mediaController?.let { controller ->
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
            }
        }

        binding.musicCard.btnNext.setOnClickListener {
            mediaController?.transportControls?.skipToNext()
        }
    }

    private fun initMediaController() {
        mediaSessionManager = requireContext().getSystemService(MediaSessionManager::class.java)

        val component = ComponentName(requireContext(), MyNotificationListener::class.java)

        // Load active session
        updateMediaController()

        // Listen for session changes (if a new app starts playing)
        mediaSessionManager.getActiveSessions(component)?.let { controllers ->
            if (controllers.isNotEmpty()) {
                mediaController = controllers[0]
                mediaController?.metadata?.let { updateUIFromMetadata(it) }
                mediaController?.registerCallback(mediaControllerCallback)
            }
        }
    }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            startSeekBarUpdater()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateUIFromMetadata(metadata)
            startSeekBarUpdater()
        }
    }


    private fun updateMediaController() {
        val controllers = mediaSessionManager.getActiveSessions(
            ComponentName(requireContext(), MyNotificationListener::class.java)
        )
        if (controllers.isNotEmpty()) {
            mediaController = controllers[0]
            mediaController?.metadata?.let { updateUIFromMetadata(it) }
            mediaController?.registerCallback(mediaControllerCallback)
        } else {
            Toast.makeText(requireContext(), "No active music session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIFromMetadata(metadata: MediaMetadata?) {
        metadata?.let {
            activity?.runOnUiThread {
                val albumArt = it.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: it.getBitmap(MediaMetadata.METADATA_KEY_ART)
                binding.musicCard.songImg.setImageBitmap(albumArt)
            }
        }
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
