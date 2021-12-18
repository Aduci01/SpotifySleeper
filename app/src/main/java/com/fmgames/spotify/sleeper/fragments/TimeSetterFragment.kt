package com.fmgames.spotify.sleeper.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_TIMER_PAUSE
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_TIMER_START_OR_RESUME
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_TIMER_STOP
import com.fmgames.spotify.sleeper.others.BasicConstants.INTENT_EXTRA_TIME
import com.fmgames.spotify.sleeper.databinding.FragmentTimeSetterBinding
import com.fmgames.spotify.sleeper.others.Tools
import com.fmgames.spotify.sleeper.services.TimerService

class TimeSetterFragment : Fragment() {
    private lateinit var binding: FragmentTimeSetterBinding
    private val TAG = "TimeSetterFragment"


    private var allSeconds = 0L
    private var currentSeconds = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeSetterBinding.inflate(LayoutInflater.from(context))

        initUi()
        subscribeToObservers()

        return binding.root
    }

    private fun initUi(){
        binding.btnHolder.visibility = View.GONE

        binding.btnPlay.setOnClickListener {
            sendCommandToService(ACTION_TIMER_START_OR_RESUME, allSeconds)
        }

        binding.btnPause.setOnClickListener {
            sendCommandToService(ACTION_TIMER_PAUSE)
        }

        binding.btnStop.setOnClickListener {
            sendCommandToService(ACTION_TIMER_STOP)
        }

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(0)
                .setMinute(10)
                .setTitleText("Set Time")
                .build()

        picker.addOnPositiveButtonClickListener {
            allSeconds = (picker.minute * 60).toLong()
            currentSeconds = allSeconds
            updateProgressBar()

            binding.btnHolder.visibility = View.VISIBLE
        }

        binding.btnSetTimer.setOnClickListener { picker.show(parentFragmentManager, TAG); }
    }

    private fun sendCommandToService(action: String, allTime: Long? = null) =
        Intent(requireContext(), TimerService::class.java).also {
            it.action = action

            if (allTime != null)
                it.putExtra(INTENT_EXTRA_TIME, allTime)

            requireContext().startService(it)
        }

    private fun subscribeToObservers() {
        TimerService.isTicking.observe(viewLifecycleOwner, Observer {
            updateTicking(it)
        })

        TimerService.allSeconds.observe(viewLifecycleOwner, Observer {
            allSeconds = it
            updateProgressBar()
        })

        TimerService.currentSeconds.observe(viewLifecycleOwner, Observer {
            currentSeconds = it
            updateProgressBar()
        })
    }

    private fun updateTicking(b: Boolean) {
        if (!b){
            binding.btnPause.visibility = View.GONE
            binding.btnStop.visibility = View.GONE
            binding.btnPlay.visibility = View.VISIBLE

            binding.divider1.visibility = View.GONE
            binding.divider2.visibility = View.GONE
        } else {
            binding.btnHolder.visibility = View.VISIBLE

            binding.btnPause.visibility = View.VISIBLE
            binding.btnStop.visibility = View.VISIBLE
            binding.btnPlay.visibility = View.GONE

            binding.divider1.visibility = View.VISIBLE
            binding.divider2.visibility = View.VISIBLE
        }
    }

    private fun updateProgressBar(){
        val formattedTime = Tools.getFormattedStopWatchTime(currentSeconds)
        binding.textRemainingTime.text = formattedTime

        if (allSeconds == 0L) {
            binding.progressIndicator.progress = 0
            binding.btnHolder.visibility = View.GONE
        }
        else binding.progressIndicator.progress = (100 - (currentSeconds.toFloat() / allSeconds.toFloat()) * 100).toInt()
    }


}