package com.dev.dockchill

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.dockchill.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = ScreenSlidePagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.pager) { _, _ ->
        }.attach()

    }

    // fragmentak slider bezala funtzionatu dezaten eta fragmentak sortzeko gure MainActivity honetan
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        //fragmentak sortu eta ezarri
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> pomodoroFragment()
                1 -> MainScreenFragment()
                else -> todo_listFragment()
            }
        }
    }
}
