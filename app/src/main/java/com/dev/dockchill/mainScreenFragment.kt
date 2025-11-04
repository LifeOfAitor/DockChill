package com.dev.dockchill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.dockchill.databinding.FragmentMainScreenBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class mainScreenFragment : Fragment() {
    private lateinit var binding: FragmentMainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orduaErakutsi()
    }

    private fun orduaErakutsi(){
        // data eta ordua ezarri
        val now = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.timeText.text = timeFormat.format(now)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateText.text = dateFormat.format(now)
    }

}