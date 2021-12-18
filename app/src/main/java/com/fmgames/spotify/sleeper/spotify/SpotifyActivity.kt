package com.fmgames.spotify.sleeper.spotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.fmgames.spotify.sleeper.R
import com.fmgames.spotify.sleeper.databinding.ActivitySpotifyBinding

class SpotifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpotifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySpotifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.fragmentContainerView)
        binding.bottomMenu.setupWithNavController(navController)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(SpotifyManager.mSpotifyAppRemote)
    }
}