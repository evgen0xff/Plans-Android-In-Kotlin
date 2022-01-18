package com.planscollective.plansapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.facebook.internal.Validate.hasPermission
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.MainActivity
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.webServices.user.UserWebService
import java.lang.Exception
import java.util.ArrayList

class GeofenceTransitionsJobIntentService : JobIntentService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cancellationTokenSource = CancellationTokenSource()


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage: String = GeofenceErrorMessages.getErrorString(
                this,
                geofencingEvent.errorCode
            )
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            updateCurrentLocation()
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition))
        }
    }

    private fun updateCurrentLocation() {
        requestCurrentLocation { location, _ ->
            location?.let {
                UserInfo.latitude = it.latitude
                UserInfo.longitude = it.longitude
                if (UserInfo.isLoggedIn && !UserInfo.userId.isNullOrEmpty()) {
                    UserWebService.updateLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation(complete: ((location: Location?, msgError: String?) -> Unit)? = null) {
        Log.d(TAG, "requestCurrentLocation()")
        if (hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    complete?.also { it(result, null) }
                } else {
                    val exception = task.exception
                    complete?.also { it(null, exception?.toString()) }
                }
            }

        }else {
            complete?.also { it(null, "Permission denied") }
        }
    }

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GeofenceTransitionsIS"

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context?, intent: Intent?) {
            enqueueWork(
                context!!,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent!!
            )
        }
    }
}

/**
 * Geofence error codes mapped to error messages.
 */
internal object GeofenceErrorMessages {
    /**
     * Returns the error string for a geofencing error code.
     */
    fun getErrorString(context: Context, errorCode: Int): String {
        val mResources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> mResources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> mResources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> mResources.getString(R.string.geofence_too_many_pending_intents)
            else -> mResources.getString(R.string.unknown_geofence_error)
        }
    }
}

