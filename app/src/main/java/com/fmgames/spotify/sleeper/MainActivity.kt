package com.fmgames.spotify.sleeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fmgames.spotify.sleeper.databinding.ActivityMainBinding
import com.fmgames.spotify.sleeper.spotify.SpotifyManager
import android.content.Intent

import android.content.ActivityNotFoundException
import android.net.Uri
import android.view.WindowManager
import com.google.android.material.snackbar.Snackbar
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_SHOW_TIMER_SCREEN
import com.fmgames.spotify.sleeper.spotify.SpotifyActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        navigateToTimerFragmentIfNeeded(intent)


        if (SpotifyManager.mSpotifyAppRemote == null || !SpotifyManager.mSpotifyAppRemote!!.isConnected){
            setupUi()
        } else { //Already connected to spotify
            loadSpotifyActivity()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTimerFragmentIfNeeded(intent)
    }

    private fun navigateToTimerFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TIMER_SCREEN) {
            loadSpotifyActivity()
        }
    }

    private fun setupUi(){
        binding.btnLogIn.setOnClickListener {
            authorizeSpotify()
            //TODO use authorization
            //loadSpotifyActivity()
        }

        binding.btnDownloadSpotify.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")))
            }
        }
    }

    private fun loadSpotifyActivity(){
        val intent = Intent(this, SpotifyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent)
    }

    private fun authorizeSpotify(){
        SpotifyManager.authorizeSpotify(
            ::onAuthorizationSuccess,
            ::onAuthorizationFailed,
            this
        )
    }

    private fun onAuthorizationFailed(throwable: Throwable){
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_LONG).show()
    }

    private fun onAuthorizationSuccess(){
        loadSpotifyActivity()
    }
}