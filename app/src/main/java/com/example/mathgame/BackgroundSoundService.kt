package com.example.itismydomain

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BackgroundSoundService : Service() {

    private var player: MediaPlayer? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "PAUSE_MUSIC" -> player?.pause()
                "RESUME_MUSIC" -> player?.start()
                "CHANGE_VOLUME" -> {
                    val volume = intent.getFloatExtra("VOLUME", 1f)
                    player?.setVolume(volume, volume)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.music)
        player?.isLooping = true

        val filter = IntentFilter().apply {
            addAction("PAUSE_MUSIC")
            addAction("RESUME_MUSIC")
            addAction("CHANGE_VOLUME")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player?.start()

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val volume = sharedPref.getFloat("SoundVolume", 100f) / 100f
        player?.setVolume(volume, volume)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
        player = null
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}