package com.planscollective.plansapp.activity

import android.Manifest
import android.content.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Size
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.ActivityDashboardBinding
import com.planscollective.plansapp.extension.setLayoutSize
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.PlansLocationManager
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.viewModels.DashboardActivityVM
import com.planscollective.plansapp.service.LocationService
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.user.UserWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import permissions.dispatcher.ktx.constructPermissionsRequest


class DashboardActivity : BaseActivity<ActivityDashboardBinding>() {

    private val viewModel: DashboardActivityVM by viewModels()

    private var locationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var locationService: LocationService? = null

    // Listens for location broadcasts from LocationService.
    private lateinit var broadcastReceiver: PlansBroadcastReceiver

    // Monitors connection to the while-in-use service.
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
            constructPermissionsRequest(Manifest.permission.ACCESS_FINE_LOCATION) {
                constructPermissionsRequest(Manifest.permission.ACCESS_COARSE_LOCATION){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        constructPermissionsRequest(Manifest.permission.ACCESS_BACKGROUND_LOCATION){
                            locationService?.subscribeToLocationUpdates() ?: println("Location Service Not Bound")
                        }.launch()
                    }else {
                        locationService?.subscribeToLocationUpdates() ?: println("Location Service Not Bound")
                    }
                }.launch()
            }.launch()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            locationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mNavController = (supportFragmentManager.findFragmentById(R.id.navHost_dashboard) as? NavHostFragment)?.navController

        setupUI()

        processIntent(intent)

        checkShareContents()
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, LocationService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            broadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (locationServiceBound) {
            unbindService(serviceConnection)
            locationServiceBound = false
        }
        super.onStop()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    override fun initialize() {
        super.initialize()
        broadcastReceiver = PlansBroadcastReceiver()
    }

    override fun setupUI() {
        super.setupUI()

        binding.layoutOverLayer.setOnSingleClickListener {
            hideOverlay()
        }
        hideOverlay()
        hideAlertMessage()
        hideWaitingMessage()
    }

    fun showPopUpForEventLived(event: EventModel?) {
        if (event == null) return
        val message = "You are live at ${event.eventName ?: ""}!"
        val resId = R.drawable.ic_live_large
        showPopUp(message, resId, Size(45.toPx(), 30.toPx()))
    }

    fun showPopUp(message: String?, imageResId: Int?, sizeImage: Size? = null) {
        binding.apply {
            tvMessage.visibility = message?.takeIf{ it.isNotEmpty()}?.let{
                tvMessage.text = it
                View.VISIBLE
            } ?: View.GONE

            imvMark.visibility = imageResId?.let {
                imvMark.setImageResource(it)
                val size = sizeImage ?: Size(50.toPx(), 50.toPx())
                imvMark.setLayoutSize(size.width, size.height)
                View.VISIBLE
            } ?: View.GONE

            layoutOverLayer.visibility = if (tvMessage.visibility == View.GONE && imvMark.visibility == View.GONE) View.GONE else View.VISIBLE
        }
    }

    override fun showOverlay(message: String?, imageResId: Int?, delayForHide: Long?, actionAfterHide: (() -> Unit)?) {
        showPopUp(message, imageResId)
        lifecycleScope.launch(Dispatchers.IO) {
            val after = delayForHide ?: 1000
            delay(after)
            withContext(Dispatchers.Main) {
                hideOverlay()
                actionAfterHide?.also { it() }
            }
        }
    }



    override fun hideOverlay() {
        super.hideOverlay()
        binding.layoutOverLayer.visibility = View.GONE
    }


    override fun showAlert(message: String?) {
        val alert = message.takeIf { !it.isNullOrEmpty() } ?: return
        binding.tvAlertMessage.text = alert
        binding.layoutAlertMessage.visibility = View.VISIBLE
    }

    override fun showWaiting(message: String?) {
        val alert = message.takeIf { !it.isNullOrEmpty() } ?: return
        binding.apply {
            tvWaitingMessage.text = alert
            spinner.isIndeterminate = true
            layoutWaiting.visibility = View.VISIBLE
        }
    }

    override fun hideAlertMessage() {
        binding.layoutAlertMessage.visibility = View.GONE
    }

    override fun hideWaitingMessage() {
        binding.spinner.isIndeterminate = false
        binding.layoutWaiting.visibility = View.GONE
    }

    private fun processIntent(intent: Intent?) {
        val bundle = intent?.extras?.takeIf { !it.isEmpty } ?: return
        bundle.getParcelable<NotificationActivityModel>("plans_notification")?.takeIf{ !it.notificationType.isNullOrEmpty() }.also {
            gotoNotification(it)
        }
    }


    private fun gotoNotification(notification: NotificationActivityModel?) {
        lifecycleScope.launch(Dispatchers.IO){
            delay(300)
            withContext(Dispatchers.Main) {
                PLANS_APP.dashboardFragment?.gotoDashboard()
                withContext(Dispatchers.IO) {
                    delay(300)
                    withContext(Dispatchers.Main) {
                        PLANS_APP.dashboardFragment?.gotoNotification(notification)
                    }
                }
            }
        }
    }


    /**
     * Receiver for location broadcasts from [LocationService].
     */
    private inner class PlansBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getParcelableExtra<Location>(LocationService.EXTRA_LOCATION)?.let {
                UserInfo.latitude = it.latitude
                UserInfo.longitude = it.longitude
                updateUserLocation()
            }
        }
    }

    fun updateUserLocation(latitude: Double? = null, longitude: Double? = null){
        val lat = latitude ?: UserInfo.latitude
        val long = longitude ?: UserInfo.longitude
        UserWebService.updateLocation(lat, long){ success, message ->
            PLANS_APP.didUpdatedUserLocation = true
            PLANS_APP.currentFragment?.refreshAll(false, true)
            PlansLocationManager.monitorRegions()
        }
    }

    fun checkShareContents() {
        println("DashboardActivity UserInfo.listShareContents count - ${UserInfo.listShareContents.count()}")
        UserInfo.listShareContents.forEach { getShareContent(it) }
    }

    fun getShareContent(content: HashMap<String, String?>?) {
        println("DashboardActivity getShareContent - $content")
        EventWebService.getShareContent(content){ result, message ->
            if (result != null) {
                UserInfo.removeShareContent(content)
                (result["statusCode"] as? Number?)?.toInt()?.also {
                    (result["message"] as? String?)?.takeIf { it.isNotEmpty() }?.also { msg ->
                        ToastHelper.showMessage(msg)
                    }
                    when(it) {
                        0, 3, 5, 201 -> {
                            showShareContent(content)
                        }
                        9 -> { // When the Post was already deleted
                            content?.remove("postId")
                            showShareContent(content)
                        }
                        10 -> { // When the Comment was already deleted
                            content?.remove("commentId")
                            showShareContent(content)
                        }
                        202 -> {
                            showShareContent(content, true)
                        }
                    }
                }
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun showShareContent(content: HashMap<String, String?>?, isFriendRequest: Boolean = false) {
        val eventId = content?.get("eventId")
        val postId = content?.get("postId")
        val hostId = content?.get("hostId")

        lifecycleScope.launch(Dispatchers.IO){
            delay(300)
            withContext(Dispatchers.Main) {
                PLANS_APP.dashboardFragment?.gotoDashboard()
                withContext(Dispatchers.IO) {
                    delay(300)
                    withContext(Dispatchers.Main) {
                        if (isFriendRequest) {
                            PLANS_APP.dashboardFragment?.gotoUserProfile(userId = hostId)
                        }else if (!eventId.isNullOrEmpty()) {
                            PLANS_APP.dashboardFragment?.gotoEventDetails(eventId = eventId)
                            if (!postId.isNullOrEmpty()){
                                withContext(Dispatchers.IO) {
                                    delay(300)
                                    withContext(Dispatchers.Main) {
                                        PLANS_APP.dashboardFragment?.gotoPostComment(eventId = eventId, postId = postId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


}
