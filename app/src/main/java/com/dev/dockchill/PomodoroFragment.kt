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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dev.dockchill.data.PomodoroDatabase
import com.dev.dockchill.data.PomodoroRepository
import kotlinx.coroutines.launch

class PomodoroFragment : Fragment() {

    private lateinit var binding: FragmentPomodoroBinding

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

        // Timerrari dagozkioten funtzioak martxan jartzeko
        setupStatisticsObservers()
        setupTimerButtons()
        initializeTimer()

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

    private fun hideMenus() {
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
    }


    private fun setupStatisticsObservers() {
        // Estatistika bisten behaketa - datu basearen aldaketak entzuten ditugu eta pantailan eguneratzen ditugunbistak
        // Observe today's pomodoros
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
        // Erlojua inicializatzen dugu - lan saioaren edo deskantsoaren arabera denbora kalkulatzen da
        // Pantailako elementuak eguneratzen ditugu eta ronda zenbakia erakusten dugu
        val durationMinutes = if (isWorkSession) pomodoroLengthMinutes else pomodoroRestMinutes
        timeLeftInMillis = durationMinutes * 60 * 1000L
        updateTimerText()
        updateCircularProgress()
        binding.roundText.text = "$pomodoroCurrentRound / $pomodoroRounds"
    }

    private fun startTimer() {
        // Kontaketa atzerakoak hasten dugu - segundu bakoitzean denbora eta progresoa eguneratzen dira
        // Saio osoa bukatzean, lan saioa datu basean gordetzen da eta deskantso saiora aldatzen da
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                updateCircularProgress()
            }

            override fun onFinish() {
                isTimerRunning = false
                binding.btnStartPause.apply { setIconResource(R.drawable.ic_play) }

                if (isWorkSession) {
                    // Lan saioa osatuta - datu basean batekin gordetzen da eta deskantso saioaren arabera aldatzen da
                    viewModel.addCompletedPomodoro(pomodoroLengthMinutes)
                    // Deskantso saioaren kaldera aldatzen da
                    isWorkSession = false
                } else {
                    // Deskantso saiooa osatuta - ronda gehitu eta beste deskantso saioak dauden konprobatu
                    pomodoroCurrentRound++
                    isWorkSession = true

                    if (pomodoroCurrentRound > pomodoroRounds) {
                        // Ronda guztiak osaturik - hasierako rondara itzuli
                        pomodoroCurrentRound = 1
                    }
                }

                // Hurrengo saioarentzat erlojua inicializatzen da
                initializeTimer()
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

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

}