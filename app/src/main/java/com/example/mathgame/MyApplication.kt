package com.example.mathgame

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.FirebaseApp

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            val intent = Intent("RESUME_MUSIC")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            val intent = Intent("PAUSE_MUSIC")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}