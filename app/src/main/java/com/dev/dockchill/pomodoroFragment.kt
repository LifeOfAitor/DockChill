package com.dev.dockchill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dev.dockchill.databinding.FragmentPomodoroBinding
import androidx.transition.TransitionManager
import androidx.core.view.isVisible
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

        binding.btnMenu.setOnClickListener {
            if (binding.pomodoroMenu.isGone) {
                // MOSTRAR
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
                // OCULTAR
                binding.pomodoroMenu.animate()
                    .alpha(0f)
                    .translationY(-50f) // Se mueve hacia arriba al desaparecer
                    .setDuration(300)
                    .withEndAction {
                        binding.pomodoroMenu.visibility = View.GONE
                    }
            }
        }
    }

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