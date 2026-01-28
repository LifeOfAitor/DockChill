package com.dev.dockchill

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.dev.dockchill.databinding.FragmentPomodoroBinding
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dev.dockchill.data.PomodoroDatabase
import com.dev.dockchill.data.PomodoroRepository
import kotlinx.coroutines.launch

class PomodoroFragment : Fragment() {

    private lateinit var binding: FragmentPomodoroBinding

    // Constantes para guardar/restaurar estado del timer
    companion object {
        private const val KEY_TIMER_RUNNING = "timer_running"
        private const val KEY_TIME_LEFT = "time_left"
        private const val KEY_LENGTH_MINUTES = "length_minutes"
        private const val KEY_REST_MINUTES = "rest_minutes"
        private const val KEY_ROUNDS = "rounds"
        private const val KEY_CURRENT_ROUND = "current_round"
        private const val KEY_IS_WORK_SESSION = "is_work_session"
    }

    // PomodoroViewModel-a Room databaseko ViewModelarekin lan egiteko, honek ahalbidetuko digu denbora errealean
    // datu baseari kontsultak egitea eta informazioa ikusi ahal izatea, estatistikak izango dira
    // guk begiratuko ditugun datuak
    private val viewModel: PomodoroViewModel by viewModels {
        val database = PomodoroDatabase.getDatabase(requireContext())
        val repository = PomodoroRepository(database.pomodoroStatsDao())
        PomodoroViewModelFactory(repository)
    }

    // CountDownTimer izango da Pomodoro fragmentuan daukagun erlojuaren kudeaketaz enkargatuko dena.
    // Bertan gure Pomodoro sesioko datuak izango ditugu, guk ezarritako denboraren arabera
    // funtzionatuko du, adibidez 30 minutu lan eta 5 minutu deskantso
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0

    //Pomodoro menuk aukerak
    private var pomodoroLengthMinutes = 25
    private var pomodoroRestMinutes = 5
    private var pomodoroRounds = 4
    private var pomodoroCurrentRound = 1
    private var isWorkSession = true

    private var isRestSession = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Fragment-aren layout-a inflatu eta binding sortu
        binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restaurar estado guardado si existe
        savedInstanceState?.let {
            isTimerRunning = it.getBoolean(KEY_TIMER_RUNNING, false)
            timeLeftInMillis = it.getLong(KEY_TIME_LEFT, 0L)
            pomodoroLengthMinutes = it.getInt(KEY_LENGTH_MINUTES, 25)
            pomodoroRestMinutes = it.getInt(KEY_REST_MINUTES, 5)
            pomodoroRounds = it.getInt(KEY_ROUNDS, 4)
            pomodoroCurrentRound = it.getInt(KEY_CURRENT_ROUND, 1)
            isWorkSession = it.getBoolean(KEY_IS_WORK_SESSION, true)
        }

        // Timerrari dagozkioten funtzioak martxan jartzeko
        setupStatisticsObservers()
        setupTimerButtons()

        // Restaurar timer si estaba en ejecución
        if (savedInstanceState != null) {
            updateTimerDisplay()
            if (isTimerRunning) {
                startTimer()
            }
        } else {
            initializeTimer()
        }

        //menuak itxi pantaiako edozein puntutan click egitean
        binding.screen.setOnClickListener { hideMenus() }

        // menuaren konportamendua animazio txiki batekin
        binding.btnMenu.setOnClickListener {
            if (binding.pomodoroMenu.isGone) {
                // menuak erakusteko
                binding.pomodoroMenu.apply {
                    alpha = 0f
                    translationY = -50f // Empieza un poco más arriba
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .translationY(0f) // Vuelve a su posición original
                        .setDuration(300)
                        .setListener(null)
                }
                binding.statsPanel.apply {
                    alpha = 0f
                    translationY = -50f // Empieza un poco más arriba
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .translationY(0f) // Vuelve a su posición original
                        .setDuration(300)
                        .setListener(null)
                }
                binding.timerContainer.apply { visibility = View.GONE }

            } else {
                // menuak ixteko
                hideMenus()
            }
        }

        // pomodoroko menuko informazioa aldatu sliderra mugitzean
        // slider bakoitzak bere balioak izango ditu, azkenean hautatutako balioak gordeko dira
        // Pomodoro kontadorera aplikatzeko

        //IRAUPENA
        binding.seekBarIraupena.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.valueIraupena.text = "$progress min"
                pomodoroLengthMinutes = progress
                if (!isTimerRunning) {
                    initializeTimer()
                }
            }

            //ez dira beharrezkoak baina utzi behar dira
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //DESKANTSOA
        binding.seekBarRest.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.valueRest.text = "$progress min"
            }

            //ez dira beharrezkoak baina utzi behar dira
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // RONDA kopurua
        binding.seekBarRondak.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.valueRondak.text = "$progress"
                binding.roundText.text = "1 / $progress"
                pomodoroRounds = progress
            }

            //ez dira beharrezkoak baina utzi behar dira
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // menua irekita badago, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    override fun onResume() {
        super.onResume()
        hideMenus()
    }


    // menuak irekita badaude, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    private fun hideMenus() {
        if (binding.pomodoroMenu.isVisible) {
            binding.pomodoroMenu.animate()
                .alpha(0f)
                .translationY(-50f) // Se mueve hacia arriba al desaparecer
                .setDuration(300)
                .withEndAction {
                    binding.pomodoroMenu.visibility = View.GONE
                }
            binding.statsPanel.animate()
                .alpha(0f)
                .translationY(-50f) // Se mueve hacia arriba al desaparecer
                .setDuration(300)
                .withEndAction {
                    binding.pomodoroMenu.visibility = View.GONE
                }
            binding.timerContainer.apply { visibility = View.VISIBLE }
        }
    }


    private fun setupStatisticsObservers() {
        // Estatistika bisten behaketa - datu basearen aldaketak entzuten ditugu eta pantailan eguneratzen ditugu
        // gaurko pomodoroak ikusteko
        lifecycleScope.launch {
            viewModel.todayPomodoros.collect { count ->
                binding.todayPomodoros.text = count.toString()
            }
        }

        // Observe current streak
        lifecycleScope.launch {
            viewModel.currentStreak.collect { streak ->
                binding.currentStreak.text = streak.toString()
            }
        }

        // Observe longest streak
        lifecycleScope.launch {
            viewModel.longestStreak.collect { streak ->
                binding.longestStreak.text = streak.toString()
            }
        }

        // Observe total pomodoros
        viewModel.totalPomodoros.observe(viewLifecycleOwner) { total ->
            binding.totalPomodoros.text = (total ?: 0).toString()
        }

        // Observe total focus time
        viewModel.totalFocusMinutes.observe(viewLifecycleOwner) { minutes ->
            val hours = (minutes ?: 0) / 60
            binding.totalFocusTime.text = hours.toString()
        }
    }

    private fun setupTimerButtons() {
        // Botoien entzulea ezartzea - hasi/pausatu eta berrezarri botoiak
        binding.btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun initializeTimer() {
        // Erlojua inizializatzen dugu - lan saioaren edo deskantsoaren arabera denbora kalkulatzen da
        // Pantailako elementuak eguneratzen ditugu eta ronda zenbakia erakusten dugu
        val durationMinutes = if (isWorkSession) pomodoroLengthMinutes else pomodoroRestMinutes
        timeLeftInMillis = durationMinutes * 60 * 1000L
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        // Actualizar la vista del timer sin reinicializar
        updateTimerText()
        updateCircularProgress()
        binding.roundText.text = "$pomodoroCurrentRound / $pomodoroRounds"
        if (isWorkSession){
            binding.txtEgoera.text = "LANEAN"
        }else{
            binding.txtEgoera.text = "ATSEDEN"
        }

        // Actualizar seekbars
        binding.seekBarIraupena.progress = pomodoroLengthMinutes
        binding.valueIraupena.text = "$pomodoroLengthMinutes min"
        binding.seekBarRest.progress = pomodoroRestMinutes
        binding.valueRest.text = "$pomodoroRestMinutes min"
        binding.seekBarRondak.progress = pomodoroRounds
        binding.valueRondak.text = "$pomodoroRounds"
    }

    private fun startTimer() {
        // Timerra sortu
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                updateCircularProgress()
            }

            override fun onFinish() {
                // automatikoki hasiko da deskantsoa edo berriro sesioa geratzen diren ronden arabera

                if (isWorkSession) {
                    // --- Lan sesioa bukatuta ---

                    // gorde datuak datubasean
                    viewModel.addCompletedPomodoro(pomodoroLengthMinutes)

                    // "deskantso" modura aldatu"
                    isWorkSession = false

                    // prestatu deskantsorako denbora
                    initializeTimer()

                    // automatikoki hasi timerra hurrengo sesiorako
                    startTimer()

                } else {
                    // --- deskantso sesioa bukatuta ---

                    // ronda gehitu
                    pomodoroCurrentRound++

                    // lan modua ezarri
                    isWorkSession = true

                    // konprobatu ea rondarik geratzen den len egiteko
                    if (pomodoroCurrentRound > pomodoroRounds) {
                        // Bukatuta badaude sesio guztiak dena gelditu
                        isTimerRunning = false
                        binding.btnStartPause.apply { setIconResource(R.drawable.ic_play) }

                        // Lehenengo rondara bueltatu aurreragorako
                        pomodoroCurrentRound = 1
                        initializeTimer()
                    } else {
                        // bestela beste sesio bat hasi
                        initializeTimer()
                        startTimer()
                    }
                }
            }
        }.start()

        isTimerRunning = true
        binding.btnStartPause.apply { setIconResource(R.drawable.ic_pause__2_) }
    }


    private fun pauseTimer() {
        // Erlojua eten egiten dugu - denborat gordetu egiten da hurrengo hasiera arte
        countDownTimer?.cancel()
        isTimerRunning = false
        binding.btnStartPause.apply { setIconResource(R.drawable.ic_play) }
    }

    private fun resetTimer() {
        // Erlojua berrezarten dugu - hasierako egoera guztietan itzuli eta lehen saioa hasten dugu
        countDownTimer?.cancel()
        isTimerRunning = false
        binding.btnStartPause.apply { setIconResource(R.drawable.ic_play) }
        isWorkSession = true
        pomodoroCurrentRound = 1
        initializeTimer()
    }

    private fun updateTimerText() {
        // Pantailan erakusten duten testua eguneratzen dugu - minutu eta segunduak MM:SS formatuan
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.timeText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateCircularProgress() {
        // Zirkular progresabarra eguneratzen dugu - denbora gelditzen den arabera ehunekoa kalkulatzen da
        val durationMinutes = if (isWorkSession) pomodoroLengthMinutes else pomodoroRestMinutes
        val totalMillis = durationMinutes * 60 * 1000L
        val progress = ((timeLeftInMillis.toFloat() / totalMillis.toFloat()) * 100).toInt()
        binding.circularProgressBar.progress = progress
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Guardar el estado del timer antes de que se destruya el fragment
        outState.putBoolean(KEY_TIMER_RUNNING, isTimerRunning)
        outState.putLong(KEY_TIME_LEFT, timeLeftInMillis)
        outState.putInt(KEY_LENGTH_MINUTES, pomodoroLengthMinutes)
        outState.putInt(KEY_REST_MINUTES, pomodoroRestMinutes)
        outState.putInt(KEY_ROUNDS, pomodoroRounds)
        outState.putInt(KEY_CURRENT_ROUND, pomodoroCurrentRound)
        outState.putBoolean(KEY_IS_WORK_SESSION, isWorkSession)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

}