package com.dev.dockchill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.dockchill.databinding.FragmentTodoListBinding

// Eginbeharrak kudeatzeko fragment-a. Eginbeharrak gehitu, editatu, ezabatu eta berrantolatu ahal izango dira hemen
// EginbeharrakRepository erabiliko da datuak fitxategi batean gordetzeko eta kargatzeko
// EginbeharrakAdapter erabiliko da RecyclerView-an eginbeharrak erakusteko
// OnEginbeharraMoveListener interfazeak eginbeharrak gora eta behera mugitzeko metodoak definitzen ditu
// Eginbeharrak "assets" karpetan "eginbeharrak.json" fitxategian gordeko dira JSON formatuan
class TodolistFragment : Fragment(), OnEginbeharraMoveListener {
    private lateinit var binding: FragmentTodoListBinding
    private lateinit var repository: EginbeharrakRepository
    private lateinit var adapter: EginbeharrakAdapter
    private lateinit var eginbeharrenLista: MutableList<Eginbeharra>

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

        // repositorioa eta eginbeharren lista hasi
        repository = EginbeharrakRepository(this.requireContext())
        // eginbeharren lista fitxategitik kargatuko dutu json fitxategitik
        eginbeharrenLista = repository.kargatuEginbeharrak()

        // Adapterra konfigurartu
        adapter = EginbeharrakAdapter(
            eginbeharrenLista,
            onEginbeharraClick = { eginbeharra, position ->
                // Si está marcado como "egina" (completado), eliminarlo
                if (eginbeharra.egina) {
                    eginbeharrenLista.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    repository.gordeEginbeharrak(eginbeharrenLista)
                    Toast.makeText(requireContext(), "Eginbeharra kendu duzun", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // ez da gertatuko baina "egina" bezala egongo balitz, gorde dagoen bezala
                    repository.gordeEginbeharrak(eginbeharrenLista)
                }
            },
            onEginbeharraItemClick = { eginbeharra, position ->
                // Eginbeharra editatzeko menua ikusiarazi
                showEditMenu(eginbeharra, position)
            },
            moveListener = this
        )

        // Recyclerview konfiguratu adapter eta layout managerrekin, horrela eginbeharrak zerrendatuta agertuko dira
        binding.viewLista.layoutManager = LinearLayoutManager(this.requireContext())
        binding.viewLista.adapter = adapter

        //menuak itxi pantaiako edozein puntutan "click" egitean
        binding.screen.setOnClickListener {
            hideMenu()
            hideEditMenu()
            showLista()
        }

        // menuaren irekieraren konportamendua animazio txiki batekin
        binding.btnMenu.setOnClickListener {
            if (binding.menuBerria.isGone) {
                // eginbehar berriaren menua erakusteko
                binding.menuBerria.apply {
                    alpha = 0f
                    translationY = -50f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setListener(null)
                }
                // atzeko planoa ezkutatu
                binding.viewLista.apply { visibility = View.GONE }
                binding.viewLista.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        binding.viewLista.visibility = View.GONE
                    }
                hideEditMenu()
            } else {
                // menuak ixteko
                hideMenu()
                showLista()
            }
        }
        // Eginbehar berriaren menuaren botoien konportamendua
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
                Toast.makeText(
                    requireContext(),
                    "Mesedez, idatzi eginbehar baten izena",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

        // Editatzeko menuaren botoien konportamendua
        binding.btnCancelEdit.setOnClickListener {
            hideEditMenu()
        }
        // Gorde edizioaren aldaketak
        binding.btnSaveEdit.setOnClickListener {
            gordeEdizioa()
        }
    }

    // zerrenda erakusteko animazioa
    private fun showLista() {
        binding.viewLista.apply {
            alpha = 0f
            translationY = -50f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setListener(null)
        }
    }

    // menua irekita badago, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    override fun onResume() {
        super.onResume()
        hideMenu()
        hideEditMenu()
    }

    // menua irekita badago, fragmentu honetara bueltatzerakoan automatikoki itxi egingo da
    private fun hideMenu() {
        if (binding.menuBerria.isVisible) {
            binding.menuBerria.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.menuBerria.visibility = View.GONE
                }

            binding.viewLista.apply { visibility = View.VISIBLE }
        }
    }

    // Eginbehar berri bat gehitzeko logika
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
        repository.gordeEginbeharrak(unekoEginbeharrak)

        // 5. Eguneratu eginbeharrenLista eta notifikatu adapterari
        eginbeharrenLista.clear()
        eginbeharrenLista.addAll(unekoEginbeharrak)
        adapter.notifyDataSetChanged()

        // 6. Garbitu input-etan testua eta itxi menua
        binding.editTextIzenburua.text?.clear()
        binding.editTextDescripcion.text?.clear()

        Toast.makeText(requireContext(), "Eginbeharra gehituta", Toast.LENGTH_SHORT).show()

        // 7. Itxi menua
        hideMenu()
        // Erakutsi zerrenda berriro
        showLista()
    }

    // Eginbeharrak gora igoteko metodoa
    override fun onMoveUp(position: Int) {
        if (position > 0) {
            eginbeharrenLista.swap(position, position - 1)
            adapter.notifyItemMoved(position, position - 1)
            repository.gordeEginbeharrak(eginbeharrenLista)
        }
    }

    // Beherako mugitzeko metodoa
    override fun onMoveDown(position: Int) {
        if (position < eginbeharrenLista.size - 1) {
            eginbeharrenLista.swap(position, position + 1)
            adapter.notifyItemMoved(position, position + 1)
            repository.gordeEginbeharrak(eginbeharrenLista)
        }
    }

    // Extension function: zerrendako elementuak trukatzeko
    private fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
        val temp = this[index1]
        this[index1] = this[index2]
        this[index2] = temp
    }

    // Variable para guardar la posición actual en edición
    private var editatzekoPosizioa: Int = -1
    private var editatzekoEginbeharra: Eginbeharra? = null

    // Abrir el panel de edición
    private fun showEditMenu(eginbeharra: Eginbeharra, position: Int) {
        // Itxi sortzeko menua irekita badago
        if (binding.menuBerria.isVisible) {
            hideMenu()
        }

        editatzekoPosizioa = position
        editatzekoEginbeharra = eginbeharra

        // Editatzeko menuan jarri eginbeharraren datuak
        binding.editPanelTitle.setText(eginbeharra.izenburua)
        binding.editPanelDescription.setText(eginbeharra.deskripzioa)

        // Eginbeharren lista 40% zabalera jarri edizio menua ondo ikusteko eta lista ere
        val layoutParams =
            binding.viewLista.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        layoutParams.matchConstraintPercentWidth = 0.4f
        binding.viewLista.layoutParams = layoutParams

        // Edizio panela erakutsi animazioarekin
        binding.editPanel.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
        }
    }

    // Edizio menua ezkutatu animazioarekin
    private fun hideEditMenu() {
        if (!binding.editPanel.isVisible) return

        binding.editPanel.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.editPanel.visibility = View.GONE

                // Berriro 100% zabalera jarri eginbeharren zerrendan
                val layoutParams =
                    binding.viewLista.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                layoutParams.matchConstraintPercentWidth = 1.0f
                binding.viewLista.layoutParams = layoutParams
            }
        editatzekoPosizioa = -1
        editatzekoEginbeharra = null
    }

    // Edizioaren aldaketak gordetzeko metodoa
    private fun gordeEdizioa() {
        if (editatzekoPosizioa < 0 || editatzekoEginbeharra == null) return

        val izenburuBerria = binding.editPanelTitle.text.toString()
        val deskripzioBerria = binding.editPanelDescription.text.toString()

        if (izenburuBerria.isBlank() || deskripzioBerria.isBlank()) {
            Toast.makeText(requireContext(), "Ez utzi eremuak hutsik", Toast.LENGTH_SHORT).show()
            return
        }

        // Eguneratu eginbeharraren datuak
        eginbeharrenLista[editatzekoPosizioa].izenburua = izenburuBerria
        eginbeharrenLista[editatzekoPosizioa].deskripzioa = deskripzioBerria

        // Gorde
        repository.gordeEginbeharrak(eginbeharrenLista)
        // Adapterrari jakinarazi aldaketaz
        adapter.notifyItemChanged(editatzekoPosizioa)
        Toast.makeText(requireContext(), "Eginbeharra eguneratuta", Toast.LENGTH_SHORT).show()
        hideEditMenu()
    }

}