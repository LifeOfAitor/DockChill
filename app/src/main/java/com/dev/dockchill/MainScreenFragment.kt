package com.dev.dockchill

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dev.dockchill.databinding.FragmentMainScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainScreenFragment : Fragment() {

    // Constantes para guardar/restaurar estado del clima
    companion object {
        private const val KEY_WEATHER_LOADED = "weather_loaded"
        private const val KEY_TODAY_TEMP = "today_temp"
        private const val KEY_TODAY_DESCRIPTION = "today_description"
        private const val KEY_TODAY_ICON = "today_icon"
        private const val KEY_TOMORROW_TEMP = "tomorrow_temp"
        private const val KEY_TOMORROW_DESCRIPTION = "tomorrow_description"
        private const val KEY_TOMORROW_ICON = "tomorrow_icon"
    }

    // Fragment-aren layout-aren binding objektua
    private lateinit var binding: FragmentMainScreenBinding

    // Eguraldiaren datuak kudeatzeko klasearen instantzia
    private lateinit var weather: Weather

    // Variables para guardar el estado del clima
    private var weatherLoaded = false
    private var todayTemp: String? = null
    private var todayDescription: String? = null
    private var todayIcon: Int = 0
    private var tomorrowTemp: String? = null
    private var tomorrowDescription: String? = null
    private var tomorrowIcon: Int = 0

    // GPS baimena eskatzeko erabiliko den kodea
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    // MediaSessionManager: gailuan martxan dauden musika aplikazioen MediaSession-ak kontrolatzeko erabiltzen da.
    // Hau erabilita, Spotify, YouTube Music, etab. bezalako app-en erreprodukzioa kontrolatu dezakezu.
    private lateinit var mediaSessionManager: MediaSessionManager


    // MediaController: unean aktibo dagoen musika aplikazioa kontrolatzeko interfazea.
    // Honek uzten dizu play, pause, next, seek… komandoak bidaltzen.
    private var mediaController: MediaController? = null

    // SeekBar eguneratzen duen coroutine job-a gordeko dugu.
    private var seekBarJob: kotlinx.coroutines.Job? = null


    // abestiaren kontrol barra martxan jarriko dugu hemendik. Erabiltzaileak barratik mugitu daiteke
    private fun setupSeekBar() {
        // Listener bat jartzen diogu seekBar-ari
        binding.musicCard.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            // Erabiltzaileak barra mugitu duenean
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Abestiaren iraupena lortzen dugu
                    val duration =
                        mediaController?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0
                    // Mugitu dugun proportzioaren arabera posizio berria kalkulatzen dugu
                    val newPosition = (duration * progress / 100)
                    // Spotify-ri posizio berrira salto egiteko eskatzen diogu
                    mediaController?.transportControls?.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // Abestia erreproduzitzen ari den bitartean barraren egoera automatikoki eguneratzen da
    private fun startSeekBarUpdater() {
        // Aurreko job-a bertan behera utzi, existitzen bada
        seekBarJob?.cancel()
        // Coroutine bat abiatu (lifecycleScope → fragment bizi den bitartean martxan)
        seekBarJob = lifecycleScope.launch {
            while (isActive) {
                // MediaController, playbackState eta metadata existitzen badira…
                val controller = mediaController
                val state = controller?.playbackState
                val metadata = controller?.metadata
                if (controller != null && state != null && metadata != null) {
                    // Abestiaren iraupena
                    val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
                    // Abestiaren uneko posizioa (ms-tan)
                    val position = state.position
                    // Progresoa kalkulatu (% moduan)
                    val progress = if (duration > 0) (position * 100 / duration).toInt() else 0

                    // UI-n barraren balioa eguneratu
                    activity?.runOnUiThread {
                        binding.musicCard.seekBar.progress = progress
                    }
                }
                // 0.5 segundoan behin eguneratzen da
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

    // fragmentu honetara bueltatzerakoan berrabiaraziko ditugu parte batzuk, bestela
    // kudeatuko dugu fragmentua eguneratzeko tasa, adibidez 5 segunduro
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onResume() {
        super.onResume()

        // Eguneratu tema ikonoa (berrabiarazpenaren ondoren ere zuzen jartzeko)
        updateThemeIcon()

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

        //tema aldatzeko
        binding.themeImg.setOnClickListener { changeTheme() }

        // Ezarri hasierako ikonoa uneko temaren arabera
        updateThemeIcon()

        // Weather klasearen instantzia sortu, eguraldiaren datuak lortzeko
        weather = Weather(requireContext())

        // Restaurar el estado del clima si existe
        savedInstanceState?.let {
            weatherLoaded = it.getBoolean(KEY_WEATHER_LOADED, false)
            if (weatherLoaded) {
                todayTemp = it.getString(KEY_TODAY_TEMP)
                todayDescription = it.getString(KEY_TODAY_DESCRIPTION)
                todayIcon = it.getInt(KEY_TODAY_ICON, R.drawable.ic_error)
                tomorrowTemp = it.getString(KEY_TOMORROW_TEMP)
                tomorrowDescription = it.getString(KEY_TOMORROW_DESCRIPTION)
                tomorrowIcon = it.getInt(KEY_TOMORROW_ICON, R.drawable.ic_error)

                // Mostrar los datos guardados
                binding.weatherCard.gaurTemperatureText.text = todayTemp
                binding.weatherCard.gaurWeatherDescription.text = todayDescription
                binding.weatherCard.gaurWeatherIcon.setImageResource(todayIcon)
                binding.weatherCard.biharTemperatureText.text = tomorrowTemp
                binding.weatherCard.biharWeatherDescription.text = tomorrowDescription
                binding.weatherCard.biharWeatherIcon.setImageResource(tomorrowIcon)
            }
        }

        // ordua eta data kargatzeko / erakusteko
        showDateTime()

        // GPSaren kokapena lortzen saiatuko gara (solo si no hay datos guardados)
        if (!weatherLoaded) {
            getLocation()
        }

        // ea dagoen mediasession bat martxan notifikazio barran, adb spotify
        MyNotificationListener.onNotificationUpdate = {
            updateMediaController()   // MediaSession berria bilatu
        }


        // Musika kontrolatzeko sistemaren hasiera
        initMediaController() // controlatzailea
        setupSeekBar() // abestiaren barra
        startSeekBarUpdater() // barraren eguneraketa kontrolatzeko

        // Play/Pause botoiaren portaera
        binding.musicCard.btnPlaypause.setOnClickListener {
            mediaController?.let { controller ->
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause() // gelditu
                } else {
                    controller.transportControls.play() // abiatu
                }
            }
        }

        // Hurrengo abestira pasa
        binding.musicCard.btnNext.setOnClickListener {
            mediaController?.transportControls?.skipToNext()
        }
    }

    //Spotify bezalako aplikazio baten MediaSession aktiboa bilatu eta kontrolatzen hasten da
    private fun initMediaController() {

        // MediaSessionManager eskuratu (sistemako zerbitzua)
        mediaSessionManager = requireContext().getSystemService(MediaSessionManager::class.java)

        // Notifikazio-listenerra identifikatzen duen ComponentName
        val component = ComponentName(requireContext(), MyNotificationListener::class.java)

        // Aktibo dauden MediaSession guztiak lortu
        updateMediaController()

        // Bat badago, kontrolatzen hasiko gara (normalean Spotify)
        mediaSessionManager.getActiveSessions(component)?.let { controllers ->
            if (controllers.isNotEmpty()) {
                // Abestiaren datuak bistaratzen hasi
                mediaController = controllers[0]
                mediaController?.metadata?.let { updateUIFromMetadata(it) }

                // PlaybackState eta metadata aldaketak entzuteko callback erregistratu
                mediaController?.registerCallback(mediaControllerCallback)
            }
        }
    }

    // abestiaren arabera ikonoa aldatuko dugu, play edo pause ikonoak jarrita
    private fun updatePlayPauseIcon(state: Int?) {
        activity?.runOnUiThread {
            when (state) {
                PlaybackState.STATE_PLAYING ->
                    binding.musicCard.btnPlaypause.setImageResource(R.drawable.ic_pause__2_)

                else ->
                    binding.musicCard.btnPlaypause.setImageResource(R.drawable.ic_play)
            }
        }
    }


    private val mediaControllerCallback = object : MediaController.Callback() {

        // Spotify-k PLAY edo PAUSE egoera aldatzen duenean deitzen da
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updatePlayPauseIcon(state?.state) // hemendik kudeatuko dugu ikonoaren aldaketa
            startSeekBarUpdater()
        }

        // Abestia aldatzen denean deitzen da (titulua, irudia, iraupena…)
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateUIFromMetadata(metadata)
            startSeekBarUpdater()
        }
    }


    // Spotify aktibo dagoen bilatzen du
    private fun updateMediaController() {
        val controllers = mediaSessionManager.getActiveSessions(
            ComponentName(requireContext(), MyNotificationListener::class.java)
        )
        if (controllers.isNotEmpty()) {
            mediaController = controllers[0]
            mediaController?.metadata?.let { updateUIFromMetadata(it) }
            mediaController?.registerCallback(mediaControllerCallback)
        } else {
            /*
            * Toast.makeText(requireContext(), "Ez dago musikarik martxan.", Toast.LENGTH_SHORT).show()
            * */
        }
    }

    // interfazeko aldagaiak eguneratuko ditugu abestiaren metadataren arabera, adibidez irudia
    private fun updateUIFromMetadata(metadata: MediaMetadata?) {
        metadata?.let {
            activity?.runOnUiThread {

                // Album art bilatzen dugu
                val albumArt = it.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: it.getBitmap(MediaMetadata.METADATA_KEY_ART)

                // eguneratzeko interfazea
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
                        todayTemp = "$temp°C"
                        todayDescription = info?.descriptionEus ?: condition
                        todayIcon = info?.iconRes ?: R.drawable.ic_error

                        binding.weatherCard.gaurTemperatureText.text = todayTemp
                        binding.weatherCard.gaurWeatherDescription.text = todayDescription
                        binding.weatherCard.gaurWeatherIcon.setImageResource(todayIcon)
                    }

                    // BIHAR: biharko iragarpena
                    val forecastTomorrow = result.forecast.forecastday[1].day
                    val tempTomorrow = forecastTomorrow.avgtemp_c
                    val conditionTomorrow = forecastTomorrow.condition.text
                    val infoTomorrow = weatherMap[biharcode]

                    activity?.runOnUiThread {
                        tomorrowTemp = "$tempTomorrow°C"
                        tomorrowDescription = infoTomorrow?.descriptionEus ?: conditionTomorrow
                        tomorrowIcon = infoTomorrow?.iconRes ?: R.drawable.ic_error

                        binding.weatherCard.biharTemperatureText.text = tomorrowTemp
                        binding.weatherCard.biharWeatherDescription.text = tomorrowDescription
                        binding.weatherCard.biharWeatherIcon.setImageResource(tomorrowIcon)

                        weatherLoaded = true
                    }

                } else {
                    // Ezin izan da API erantzunik jaso (null), errorea log-ean eta UI-n erakutsi
                    Log.e("aitor", "WeatherResponse nulo {lat}, {lon}")
                    activity?.runOnUiThread {
                        erakutsiErrorea()
                    }
                }
            }

        } else {
            // Ez da GPS kokapena lortu (null) -> mezu bat erakutsi eta UI-n errorea jarri
            /*
            * Toast.makeText(requireContext(), "Ezin izan da lortu GPS", Toast.LENGTH_SHORT).show()
            */
            Log.e("aitor", "Ezin izan da lortu GPS")
            erakutsiErrorea()
        }
    }

    private fun erakutsiErrorea() {
        //errore ikonoen kolorea val honetan gordeko dut gerorago ezartzeko
        binding.weatherCard.gaurTemperatureText.text = "Error °C"
        binding.weatherCard.gaurWeatherDescription.text = "Error"
        binding.weatherCard.biharTemperatureText.text = "Error °C"
        binding.weatherCard.biharWeatherDescription.text = "Error"
        binding.weatherCard.gaurWeatherIcon.setImageResource(R.drawable.ic_error)
        binding.weatherCard.biharWeatherIcon.setImageResource(R.drawable.ic_error)
    }

    //Aplikazioaren tema aldatzeko funtzioa
    private fun changeTheme() {
        // Aldatu tema (argia <-> iluna)
        ThemeManager.toggleTheme(requireContext())
        // Nota: Activity-a berrabiaraziko da automatikoki tema aldatzean
    }

    /**
     * Eguneratu tema ikonoa uneko temaren arabera
     */
    private fun updateThemeIcon() {
        val isDarkMode = ThemeManager.isDarkModeActive()
        if (isDarkMode) {
            // Ilun moduan bagaude, erakutsi "argi" ikonoa (aldatzeko)
            binding.themeImg.setImageResource(R.drawable.ic_light)
        } else {
            // Argi moduan bagaude, erakutsi "ilun" ikonoa (aldatzeko)
            binding.themeImg.setImageResource(R.drawable.ic_dark)
        }
    }
    // eguraldiaren datuak gordetzen ditugu, fragmentu hau berriro sortzen denean berreskuratu ahal izateko
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Guardar el estado del clima antes de que se destruya el fragment
        outState.putBoolean(KEY_WEATHER_LOADED, weatherLoaded)
        if (weatherLoaded) {
            outState.putString(KEY_TODAY_TEMP, todayTemp)
            outState.putString(KEY_TODAY_DESCRIPTION, todayDescription)
            outState.putInt(KEY_TODAY_ICON, todayIcon)
            outState.putString(KEY_TOMORROW_TEMP, tomorrowTemp)
            outState.putString(KEY_TOMORROW_DESCRIPTION, tomorrowDescription)
            outState.putInt(KEY_TOMORROW_ICON, tomorrowIcon)
        }
    }

}
