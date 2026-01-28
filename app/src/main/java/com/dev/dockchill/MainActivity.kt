package com.dev.dockchill

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.dockchill.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplikatu gordetako tema hasiera batean, UI kargatu baino lehen
        val savedTheme = ThemeManager.getSavedThemeMode(this)
        ThemeManager.applyTheme(savedTheme)

        super.onCreate(savedInstanceState)
        // Ertzetaraino marrazteko gaitasuna aktibatu
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = ScreenSlidePagerAdapter(this)

        // Defektuz "1" posizioan dagoen fragmentua kargatuko du
        binding.pager.currentItem = 1

        // **MODU IMMERSIBOA AKTIBATU:** // Botoiak eta egoera barra ezkutatu, irristatuz agertuko dira (swipe).
        enterImmersiveMode()
    }

    override fun onResume() {
        super.onResume()
        // Garrantzitsua da deitzea onResume-n, erabiltzaileak aplikaziora itzultzean
        // ezkutuko egoera mantentzeko.
        enterImmersiveMode()
    }

    /**
     * Modu inmersiboa aktibatzen du. Sistemako barra guztiak ezkutatzen ditu
     * eta irristatuz (swipe) berriz agertzea ahalbidetzen du, baina ezkutuan mantentzen da.
     */
    private fun enterImmersiveMode() {
        // Leihoaren eragina kudeatzeko kontrolagailua lortu
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // 1. Nabigazio-barra (Home, Atzera, Multitarea botoiak) ezkutatu
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        // 2. Egoera-barra (Ordua, Jakinarazpenak) ezkutatu
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // 3. Jokabidea konfiguratu:
        // Irristatze labur batekin (swipe) agertu eta gero berriro ezkutatu
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // fragmentak slider bezala funtzionatu dezaten eta fragmentak sortzeko gure MainActivity honetan
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        //fragmentak sortu eta ezarri
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PomodoroFragment() // Fragmentua 0. posizioan
                1 -> MainScreenFragment() // Fragmentua 1. posizioan (Defektuzkoa)
                else -> TodolistFragment() // Fragmentua 2. posizioan
            }
        }
    }
}