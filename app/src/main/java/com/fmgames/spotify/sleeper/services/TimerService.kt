package com.fmgames.spotify.sleeper.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.fmgames.spotify.sleeper.MainActivity
import com.fmgames.spotify.sleeper.R
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_SHOW_TIMER_SCREEN
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_TIMER_PAUSE
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_TIMER_START_OR_RESUME
import com.fmgames.spotify.sleeper.others.BasicConstants.ACTION_TIMER_STOP
import com.fmgames.spotify.sleeper.others.BasicConstants.INTENT_EXTRA_TIME
import com.fmgames.spotify.sleeper.others.BasicConstants.NOTIFICATION_CHANNEL_ID
import com.fmgames.spotify.sleeper.others.BasicConstants.NOTIFICATION_CHANNEL_NAME
import com.fmgames.spotify.sleeper.others.BasicConstants.NOTIFICATION_ID
import com.fmgames.spotify.sleeper.others.Tools
import com.fmgames.spotify.sleeper.spotify.SpotifyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : LifecycleService() {
    private val TAG = "TimerService"

    var isStarted = false

    lateinit var notificationBuilder: NotificationCompat.Builder

    companion object {
        val isTicking = MutableLiveData<Boolean>()

        val currentSeconds = MutableLiveData<Long>()
        val allSeconds = MutableLiveData<Long>()
    }

    private fun setInitialValues(){
        isTicking.value = false

        currentSeconds.value = 0
        allSeconds.value = 0
    }

    override fun onCreate() {
        super.onCreate()

        notificationBuilder = getBaseNotificationBuilder()

        isTicking.observe(this, Observer {
            updateNotification(it)
        })

        setInitialValues()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action){
                ACTION_TIMER_START_OR_RESUME -> {
                    if (isStarted){
                        startTimer()
                    } else {
                        if (it.hasExtra(INTENT_EXTRA_TIME)){
                            allSeconds.value = it.getLongExtra(INTENT_EXTRA_TIME, 300)
                            currentSeconds.value = it.getLongExtra(INTENT_EXTRA_TIME, 300)
                        }

                        startForegroundService()
                        isStarted = true
                    }
                }

                ACTION_TIMER_PAUSE -> {
                    pauseService()
                }

                ACTION_TIMER_STOP -> {
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        isTicking.value = true
        Log.d(TAG, "Timer started")

        CoroutineScope(Dispatchers.Main).launch {
            while (isTicking.value!!) {
                if (currentSeconds.value!! <= 0){
                    pauseSpotify()
                    killService()
                } else {
                    currentSeconds.postValue(currentSeconds.value!! - 1)

                    delay(1000)
                }
            }
        }
    }

    private fun pauseSpotify(){
        SpotifyManager.tryToPause(this)
    }

    private fun pauseService() {
        isTicking.postValue(false)
        Log.d(TAG, "Service paused")
    }

    private fun startForegroundService(){
        startTimer()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        currentSeconds.observe(this, Observer {
            val notification = notificationBuilder
                .setContentText(Tools.getFormattedStopWatchTime(it))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        })

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        Log.d(TAG, "Service started")
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TIMER_SCREEN
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun updateNotification(isTicking: Boolean){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(notificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (isStarted) {
            notificationBuilder = getBaseNotificationBuilder()
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun killService() {
        isStarted = false
        pauseService()
        setInitialValues()
        stopForeground(true)
        stopSelf()

        Log.d(TAG, "Service killed")
    }

    private fun getBaseNotificationBuilder() : NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_bedtime_24)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText("00:00")
            .setContentIntent(getMainActivityPendingIntent())
    }
}