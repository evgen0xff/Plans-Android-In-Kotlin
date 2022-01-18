package com.planscollective.plansapp.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.broadcastReceiver.GeofenceBroadcastReceiver
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.webServices.event.EventWebService
import permissions.dispatcher.ktx.constructPermissionsRequest
import java.util.*
import kotlin.collections.ArrayList

object PlansLocationManager {

    @SuppressLint("StaticFieldLeak")
    private var mGeofencingClient: GeofencingClient = LocationServices.getGeofencingClient(PLANS_APP)

    private var mGeofenceList = ArrayList<Geofence>()

    private var mGeofencePendingIntent: PendingIntent? = null

    private var defaultInterval: Long = 1 * 30 * 1000
    private var lastTimeClicked: Long = 0


    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList)

        // Return a GeofencingRequest.
        return builder.build()
    }

    private fun checkPermissions(): Boolean {
        val formalizeForeground = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    PLANS_APP, Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val formalizeBackground =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    PLANS_APP, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
        return formalizeForeground && formalizeBackground
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private fun performPendingGeofenceTask(isAdd: Boolean = true, complete: (() -> Unit)? = null ) {
        if (isAdd) {
            addGeofences(complete)
        } else {
            removeGeofences(complete)
        }
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressLint("MissingPermission")
    private fun addGeofences(complete: (() -> Unit)? = null ) {
        if (!checkPermissions()) {
            complete?.also { it() }
            return
        }

        getGeofencePendingIntent()?.takeIf { !mGeofenceList.isEmpty() }?.also { it ->
            mGeofencingClient.addGeofences(getGeofencingRequest(), it).run {
                addOnSuccessListener {
                    // Geofences added
                    UserInfo.listAllEventAttendIDs = mGeofenceList.map { it.requestId }.toArrayList()
                    complete?.also { it() }
                }
                addOnFailureListener {
                    // Failed to add geofences
                    complete?.also { it() }
                }
            }
        } ?: run {
            complete?.also { it() }
        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences(complete: (() -> Unit)? = null) {
        if (!checkPermissions()) {
            complete?.also { it() }
            return
        }

        UserInfo.listAllEventAttendIDs.takeIf { !it.isEmpty() }?.also {
            mGeofencingClient.removeGeofences(it).run {
                addOnSuccessListener {
                    // Geofences removed
                    UserInfo.listAllEventAttendIDs = ArrayList()
                    complete?.also { it() }
                }
                addOnFailureListener {
                    // Failed to remove geofences
                    complete?.also { it() }
                }
            }
        } ?: run {
            complete?.also { it() }
        }
    }


    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private fun getGeofencePendingIntent(): PendingIntent? {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }

        val intent = Intent(PLANS_APP, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        val flagPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

        mGeofencePendingIntent =
            PendingIntent.getBroadcast(PLANS_APP, 0, intent, flagPendingIntent)
        return mGeofencePendingIntent
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    private fun populateGeofenceList(list: List<EventModel>?) {
        val events = list ?: return

        mGeofenceList.clear()

        events.forEach {
            if ( !it.id.isNullOrEmpty() && it.lat != null && it.long != null ) {
                val radius = it.checkInRange?.toFloat() ?: 300.0f
                mGeofenceList.add(
                    Geofence.Builder() // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(it.id!!) // Set the circular region of this geofence.
                        .setCircularRegion(
                            it.lat!!.toDouble(),
                            it.long!!.toDouble(),
                            radius
                        ) // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE) // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_ENTER or
                                    Geofence.GEOFENCE_TRANSITION_EXIT
                        ) // Create the geofence.
                        .setNotificationResponsiveness(1 * 60 * 1000)
                        .setLoiteringDelay(1 * 60 * 1000)
                        .build()
                )

            }
        }
    }

    private fun updateRegions() {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()

        EventWebService.getAllEventsAttended { list, _ ->
            list?.also {
                performPendingGeofenceTask(false) {
                    populateGeofenceList(it)
                    performPendingGeofenceTask(true)
                }
            }

            if (list?.any { it.isLiveUser(UserInfo.userId) } == true) {
                AnalyticsManager.logEvent(AnalyticsManager.EventType.LIVE_USER)
            }
        }
    }

    fun monitorRegions() {
        if (checkPermissions() && UserInfo.isLoggedIn && !UserInfo.userId.isNullOrEmpty()) {
            updateRegions()
        }else {
            performPendingGeofenceTask(false)
        }
    }


}

