package com.dev.dockchill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.view.Gravity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.dev.dockchill.databinding.FragmentTodoListBinding
import com.dev.dockchill.databinding.CustomToastBinding

class TodolistFragment : Fragment() {
    private lateinit var binding: FragmentTodoListBinding
    private lateinit var bindingToast: CustomToastBinding
    private lateinit var repository: EginbeharrakRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Fragment-aren layout-a inflatu eta binding sortu
        binding = FragmentTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EginbeharrakRepository(this.requireContext())

        //menuak itxi pantaiako edozein puntutan click egitean
        binding.screen.setOnClickListener { hideMenu() }
        // menuaren irekieraren konportamendua animazio txiki batekin
        binding.btnMenu.setOnClickListener {
            if (binding.addtodomenu.isGone) {
                // menuak erakusteko
                binding.addtodomenu.apply {
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
                hideMenu()
            }
        }
        binding.btnAddEginbeharra.setOnClickListener {
            // Lortu erabiltzaileak idatzitako testua
            val eginbeharIzena = binding.editTextIzenburua.text.toString()
            val eginbeharDeskripzioa = binding.editTextDescripcion.text.toString()

            // Ziurtatu testua ez dagoela hutsik
            if (eginbeharIzena.isNotBlank() && eginbeharDeskripzioa.isNotBlank()) {
                // Deitu metodo laguntzaileari logika guztia kudeatzeko
                gehituEginbeharBerria(eginbeharIzena, eginbeharDeskripzioa)
            } else {
                // Erabiltzaileari jakinarazi zerbait idatzi behar duela
                //Toast.makeText(this, "Mesedez, idatzi eginbehar baten izena", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // menua irekita badago, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    override fun onResume() {
        super.onResume()
        hideMenu()
    }

    // menua irekita badago, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    private fun hideMenu() {
        if (binding.addtodomenu.isVisible) {
            binding.addtodomenu.animate()
                .alpha(0f)
                .translationY(-50f) // Se mueve hacia arriba al desaparecer
                .setDuration(300)
                .withEndAction {
                    binding.addtodomenu.visibility = View.GONE
                }

            binding.viewLista.apply { visibility = View.VISIBLE }
        }
    }

    private fun gehituEginbeharBerria(eginbeharIzena: String, eginbeharDeskripzioa: String) {
        // 1. Kargatu uneko zerrenda osoa fitxategitik
        val unekoEginbeharrak = repository.kargatuEginbeharrak()

        // 2. Sortu eginbehar berria
        val eginbeharBerria = Eginbeharra(
            eginbeharIzena, eginbeharDeskripzioa, false
        )

        // 3. Gehitu eginbehar berria zerrendara
        unekoEginbeharrak.add(eginbeharBerria)

        // 4. Gorde zerrenda osoa eta eguneratua fitxategian
        repository.guardarEginbeharrak(unekoEginbeharrak)

        showCustomToast(this, "$eginbeharIzena eginbeharra gehituta")


        // 5. Itxi Activity hau eta itzuli aurrekora (EginbeharrekoakActivity)
        hideMenu()
    }

    //custom toast erabiltzeko, horrela mezua hobeto egongo da
    fun showCustomToast(context: TodolistFragment, message: String) {
        val inflater = LayoutInflater.from(this.context)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        // ezarri ikonoa eta textua
        val icon = bindingToast.toastIcon.setImageResource(R.drawable.icc_added)
        bindingToast.toastText.text = message

        // Crear y mostrar Toast
        with(Toast(this.context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            show()
        }
    }
}