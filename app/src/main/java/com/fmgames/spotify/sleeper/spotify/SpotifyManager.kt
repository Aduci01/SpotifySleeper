package com.fmgames.spotify.sleeper.spotify

import android.content.Context
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlin.reflect.KFunction1


object SpotifyManager {
    private const val CLIENT_ID = "a899295a559c43f9be24637cdabac1cf"
    private const val REDIRECT_URI = "comspotifytestsdk://callback"
    var mSpotifyAppRemote: SpotifyAppRemote? = null

    fun authorizeSpotify(onSuccess: () -> Unit, onFailure: (throwable: Throwable) -> Unit, context: Context){
        val connectionParams = ConnectionParams.Builder(SpotifyManager.CLIENT_ID)
            .setRedirectUri(SpotifyManager.REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("MainActivity", "Connected!")

                    connected()
                    onSuccess()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MyActivity", throwable.message, throwable)
                    onFailure(throwable)
                }
            })
    }


    fun connected() {
        // Subscribe to PlayerState
        mSpotifyAppRemote!!.playerApi
            .subscribeToPlayerState()
            .setEventCallback { playerState: PlayerState ->
                val track: Track? = playerState.track
                if (track != null) {
                    Log.d("MainActivity", track.name.toString() + " by " + track.artist.name)
                }
            }
    }

    fun tryToStartPlaylist(uri: String, context: Context){
        if (mSpotifyAppRemote?.isConnected == true){
            startPlaylist(uri)
        } else {
            authorizeSpotify(
                { startPlaylist(uri) },
                {},
                context
            )
        }
    }

    fun tryToPause(context: Context){
        if (mSpotifyAppRemote?.isConnected == true){
            pausePlay()
        } else {
            authorizeSpotify(
                ::pausePlay,
                {},
                context
            )
        }
    }

    private fun startPlaylist(uri: String){
        mSpotifyAppRemote!!.playerApi.play(uri)
    }

    private fun pausePlay(){
        mSpotifyAppRemote?.playerApi?.pause()
    }
}