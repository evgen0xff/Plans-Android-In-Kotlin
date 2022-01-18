package com.planscollective.plansapp

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.setAdvertiserIDCollectionEnabled
import com.facebook.FacebookSdk.setAutoLogAppEventsEnabled
import com.facebook.appevents.AppEventsLogger
import com.google.android.libraries.places.api.Places
import com.planscollective.plansapp.activity.BaseActivity
import com.planscollective.plansapp.activity.DashboardActivity
import com.planscollective.plansapp.activity.MainActivity
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.customUI.LoadingView
import com.planscollective.plansapp.fragment.auth.ConfirmCodeFragment
import com.planscollective.plansapp.fragment.base.BaseFragment
import com.planscollective.plansapp.fragment.dashboard.DashboardFragment
import com.planscollective.plansapp.fragment.main.LandingFragment
import com.planscollective.plansapp.fragment.signup.*
import com.planscollective.plansapp.fragment.utils.PlansDialogFragment
import com.planscollective.plansapp.helper.ContactsHelper
import com.planscollective.plansapp.helper.FileHelper
import com.planscollective.plansapp.helper.NotificationUtils
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.PlansLocationManager
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.webServices.chat.ChatService
import com.planscollective.plansapp.webServices.event.EventWebService
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val PLANS_APP = PlansApp.instance

class PlansApp : Application() {

    companion object {
        lateinit var instance: PlansApp
    }

    var currentActivity: BaseActivity<*>? = null
    var currentFragment: BaseFragment<*>? = null
    var previousFragment: BaseFragment<*>? = null
    var dashboardFragment: DashboardFragment? = null
    var hostedEvents = ArrayList<EventModel>()
    var listDeletedEventIDs = ArrayList<String>()
    var listCancelExpiredEventIDs = ArrayList<String>()
    var listBlockedUserIDs = ArrayList<String>()
    var didUpdatedUserLocation = false
    var dialogPopupEndEvent: PlansDialogFragment? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        initializeForLaunch()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }


    fun initializeForLaunch () {
        //******************* Google Place APIs *********************//
        Places.initialize(PLANS_APP, Constants.GOOGLE_API_KEY)

        //***************** Facebook SDK ****************************//
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        setAutoLogAppEventsEnabled(true)
        setAdvertiserIDCollectionEnabled(true)
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()

        //****************** App Local Data ************************//
        UserInfo.initializeForLaunch()

        //****************** App UIs *******************************//
        initializeForUIs()

        //******************** Contacts List ************************//
        ContactsHelper.getContacts(this)

        //******************* File Manager **************************//
        FileHelper.removeAllFilesInCameraFolder()
    }

    fun initializeForUIs() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            LoadingView(context, LoadingView.LoadingType.TYPE_PULL_TO_REFRESH)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            LoadingView(context, LoadingView.LoadingType.TYPE_PULL_TO_REFRESH)
        }
    }

    fun initializeForNewDashboard() {
        ChatService.initialize()
    }

    fun initializeForLogout() {
        UserInfo.initializeUserInfo()
        ChatService.initializeForLogout()
        NotificationUtils.cancelNotification(isAll = true)
        PlansLocationManager.monitorRegions()

        listDeletedEventIDs.clear()
        listCancelExpiredEventIDs.clear()
        listBlockedUserIDs.clear()
    }

    fun gotoDashboardActivity(user: UserModel? = null, message: String? = null, notification: NotificationActivityModel? = null){
        currentActivity?.also {
            val action = {

                initializeForNewDashboard()

                val i = Intent(it, DashboardActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                notification?.also { notify ->
                    i.putExtra("plans_notification", notify)
                }
                it.startActivity(i)
                it.finish()
            }
            if (message.isNullOrEmpty()) {
                action()
            }else {
                it.showOverlay(message, actionAfterHide = action)
            }
        }
    }

    fun gotoMainActivity(resIdStartDestination: Int? = null, message: String? = null){
        currentActivity?.also {
            val action = {

                initializeForLogout()

                val intent = Intent(it, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                resIdStartDestination?.also { id ->
                    intent.putExtra("resIdStartDestination", id)
                }
                it.startActivity(intent)
                it.finish()
            }

            if (message.isNullOrEmpty()) {
                action()
            }else {
                it.showOverlay(message, actionAfterHide = action)
            }
        }
    }

    fun pushNextStepForSignup(user: UserModel?, fragment: Fragment?, isSkipModel: Boolean = false) {
        var actionId : Int? = null
        if (isSkipModel) {
            if (user?.mobile.isNullOrEmpty()) {
                actionId = R.id.action_landingFragment_to_nav_signup
            }else if (user?.firstName.isNullOrEmpty() || user?.lastName.isNullOrEmpty()){
                actionId = R.id.action_global_signupNameFragment
            }else if (user?.email.isNullOrEmpty()){
                actionId = R.id.action_global_signupEmailFragment
            }else if (user?.dob == null || user.dob?.toInt() == 0 ) {
                actionId = R.id.action_global_signupBirthdayFragment
            }else if (user.password.isNullOrEmpty()) {
                actionId = R.id.action_global_signupPasswordFragment
            }else if (user.profileImage.isNullOrEmpty()){
                actionId = R.id.action_global_signupUserImageFragment
            }else {
                gotoDashboardActivity(user)
            }
        }else {
            when(fragment) {
                is LandingFragment -> {
                    actionId = R.id.action_landingFragment_to_nav_signup
                }
                is ConfirmCodeFragment -> {
                    actionId = R.id.action_global_signupNameFragment
                }
                is SignupNameFragment -> {
                    actionId = R.id.action_global_signupEmailFragment
                }
                is SignupEmailFragment -> {
                    actionId = R.id.action_global_signupBirthdayFragment
                }
                is SignupBirthdayFragment -> {
                    actionId = R.id.action_global_signupPasswordFragment
                }
                is SignupPasswordFragment -> {
                    actionId = R.id.action_global_signupUserImageFragment
                }
                is SignupUserImageFragment -> {
                    gotoDashboardActivity(user)
                }
            }
        }

        if (actionId != null) {
            (fragment as? BaseFragment<*>)?.navigate(actionId)
        }
    }

    fun refreshCurrentScreen() {
        currentFragment?.apply {
            lifecycleScope.launch(Dispatchers.Main){
                refreshAll()
            }
        }
    }

    fun getLivedEventsForEnding() {
        hostedEvents.clear()
        dialogPopupEndEvent?.dismiss()

        if (!UserInfo.isLoggedIn) return

        EventWebService.getLivedEventsForEnding { list, message ->
            if (list != null) {
                hostedEvents.addAll(list)
                showPopupEndEvent()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun showPopupEndEvent(index: Int = 0) {
        currentActivity?.lifecycleScope?.launch(Dispatchers.Main){
            if (index >= hostedEvents.size) {
                hostedEvents.clear()
                dialogPopupEndEvent?.dismiss()
                return@launch
            }
            dialogPopupEndEvent = currentActivity?.showPopupEndEvent(hostedEvents[index]){
                dialogPopupEndEvent = null
                showPopupEndEvent(index + 1)
            }
        }
    }

    fun checkForBack(): Boolean {
        val eventId = currentFragment?.curBackStackEntry?.savedStateHandle?.get<String>("eventId")
        val userId = currentFragment?.curBackStackEntry?.savedStateHandle?.get<String>("userId")
        val argEventId = currentFragment?.curBackStackEntry?.arguments?.getString("eventId")
        val argUserId = currentFragment?.curBackStackEntry?.arguments?.getString("userId")

        val isBack = if (!argEventId.isNullOrEmpty() && listDeletedEventIDs.contains(argEventId)) {
            true
        }else if (!eventId.isNullOrEmpty() && !userId.isNullOrEmpty() && userId != UserInfo.userId && listCancelExpiredEventIDs.contains(eventId)) {
            true
        }else if (!userId.isNullOrEmpty() && listBlockedUserIDs.contains(userId)) {
            true
        }else !argUserId.isNullOrEmpty() && listBlockedUserIDs.contains(argUserId)

        currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
            if (isBack) {
                currentFragment?.gotoBack()
            }
        }

        return isBack
    }

    fun addDeletedEventIDs(eventId: String?) {
        eventId?.takeIf { it.isNotEmpty() && !listDeletedEventIDs.contains(it) }?.also {
            listDeletedEventIDs.add(it)
            Handler(Looper.getMainLooper()).postDelayed({
                listDeletedEventIDs.removeAll { it == eventId }
            }, 2000)
        }
    }

    fun addCancelExpiredEventIDs(eventId: String?) {
        eventId?.takeIf { it.isNotEmpty() && !listDeletedEventIDs.contains(it) && !listCancelExpiredEventIDs.contains(it) }?.also {
            listCancelExpiredEventIDs.add(it)
            Handler(Looper.getMainLooper()).postDelayed({
                listCancelExpiredEventIDs.removeAll { it == eventId }
            }, 2000)
        }
    }

    fun addBlockedUserIDs(userId: String?) {
        userId?.takeIf { it.isNotEmpty() && !listBlockedUserIDs.contains(it) }?.also {
            listBlockedUserIDs.add(it)
            Handler(Looper.getMainLooper()).postDelayed({
                listBlockedUserIDs.removeAll { it == userId }
            }, 2000)
        }
    }

    fun gotoBack() {
        currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
            currentFragment?.gotoBack()
        }
    }

    fun updateLocation() {
        currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
            (currentActivity as? DashboardActivity?)?.updateUserLocation()
        }
    }

    fun updateBadges() {
        currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
            dashboardFragment?.updateBadges()
        }
    }

}