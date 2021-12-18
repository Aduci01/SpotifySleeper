package com.fmgames.spotify.sleeper.others

import java.util.concurrent.TimeUnit

object Tools {
    fun getFormattedStopWatchTime(sec: Long): String {
        var milliseconds = sec * 1000

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds"
    }
}