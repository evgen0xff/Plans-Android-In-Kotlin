package com.planscollective.plansapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.MainActivity
import com.planscollective.plansapp.extension.getCountryNameFromAddress
import com.planscollective.plansapp.extension.toText
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.webServices.place.PlaceWebservice
import java.util.concurrent.TimeUnit

/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
class LocationService : Service() {
    /*
    * Checks whether the bound activity has really gone away (foreground service with notification
    * created) or simply orientation change (no-op).
    */
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    override fun onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.SECONDS.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation

                currentLocation?.latitude?.also {
                    UserInfo.latitude = it
                }
                currentLocation?.longitude?.also {
                    UserInfo.longitude = it
                }

                Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST).apply{
                    putExtra(EXTRA_LOCATION, currentLocation)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(this)
                }

                PlaceWebservice.getAddressFrom(currentLocation?.latitude, currentLocation?.longitude, applicationContext) {
                    address, message ->
                    if (address != null) {
                        UserInfo.address = address
                        UserInfo.userAddressLocality = address.locality
                        UserInfo.userAddressAdminArea = address.adminArea
                        UserInfo.userAddress = address.maxAddressLineIndex.takeIf { it >= 0 }?.let { address.getAddressLine(0)}
                        UserInfo.userLocationName = UserInfo.userAddress?.split(", ")?.let {
                            if (it.size > 1) {
                                it.subList(0, 2).joinToString(", ")
                            }else {
                                it.firstOrNull()
                            }
                        }
                        UserInfo.countryOwn = UserInfo.userAddress?.getCountryNameFromAddress()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")

        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification == true) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }

        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind()")

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && UserInfo.enableLocationUpdates) {
            Log.d(TAG, "Start foreground service")
            serviceRunningInForeground = true
        }
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")

        UserInfo.enableLocationUpdates = true

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, LocationService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }catch (unlikely: SecurityException){
            UserInfo.enableLocationUpdates = false
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener{
                if (it.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                }else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            UserInfo.enableLocationUpdates = false
        }catch (unlikely: SecurityException) {
            UserInfo.enableLocationUpdates = true
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }


    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }

    companion object {
        private const val TAG = "LocationService"
        private const val PACKAGE_NAME = "com.planscollective.plansapp"
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
    }


}