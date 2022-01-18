package com.planscollective.plansapp.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.planscollective.plansapp.service.GeofenceTransitionsJobIntentService

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        // Enqueues a JobIntentService passing the context and intent as parameters
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}