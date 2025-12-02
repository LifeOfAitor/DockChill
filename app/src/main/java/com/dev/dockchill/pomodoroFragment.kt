package com.dev.dockchill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.dev.dockchill.databinding.FragmentPomodoroBinding
import androidx.core.view.isGone

class PomodoroFragment : Fragment() {

    private lateinit var binding: FragmentPomodoroBinding

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

        // menuaren konportamendua animazio txiki batekin
        binding.btnMenu.setOnClickListener {
            if (binding.pomodoroMenu.isGone) {
                // menua erakusteko
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
            } else {
                // menua ixteko
                binding.pomodoroMenu.animate()
                    .alpha(0f)
                    .translationY(-50f) // Se mueve hacia arriba al desaparecer
                    .setDuration(300)
                    .withEndAction {
                        binding.pomodoroMenu.visibility = View.GONE
                    }
            }
        }

        // pomodoroko menuko informazioa aldatu sliderra mugitzean
        // slider bakoitzak bere balioak izango ditu, azkenean hautatutako balioak gordeko dira
        // Pomodoro kontadorera aplikatzeko

        //IRAUPENA
        binding.seekBarIraupena.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.valueIraupena.text = "$progress min"
            }
            //ez dira beharrezkoak baina utzi behar dira
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //DESKANTSOA
        binding.seekBarRest.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.valueRest.text = "$progress min"
            }
            //ez dira beharrezkoak baina utzi behar dira
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // RONDA kopurua
        binding.seekBarRondak.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.valueRondak.text = "$progress"
            }
            //ez dira beharrezkoak baina utzi behar dira
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // menua irekita badago, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    override fun onResume() {
        super.onResume()
        binding.pomodoroMenu.animate()
            .alpha(0f)
            .translationY(-50f) // Se mueve hacia arriba al desaparecer
            .setDuration(300)
            .withEndAction {
                binding.pomodoroMenu.visibility = View.GONE
            }
    }

}