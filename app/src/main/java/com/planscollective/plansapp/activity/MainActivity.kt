package com.planscollective.plansapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.ActivityMainBinding
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.viewModels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : BaseActivity<ActivityMainBinding>(), ViewTreeObserver.OnGlobalLayoutListener {
    private val authVM: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mNavController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as? NavHostFragment)?.navController

        processIntent(intent)
        setupUI()
        gotoFirstScreen()
    }

    override fun setupUI() {
        super.setupUI()

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(this)

        hideOverlay()
        hideAlertMessage()
        hideWaitingMessage()
    }

    override fun showOverlay(message: String?, imageResId: Int?, delay: Long?, actionAfterHide: (() -> Unit)?) {
        super.showOverlay(message, imageResId, delay, actionAfterHide)

        message?.let {
            binding.tvMessage.text = it
        }
        imageResId?.let {
            binding.imvMark.setImageResource(it)
        }
        binding.layoutOverLayer.visibility = View.VISIBLE

        val after = delay ?: 1000
        lifecycleScope.launch(Dispatchers.IO) {
            delay(after)
            withContext(Dispatchers.Main) {
                hideOverlay()
                actionAfterHide?.also { it() }
            }
        }
    }

    override fun hideOverlay() {
        super.hideOverlay()
        binding.layoutOverLayer.visibility = View.INVISIBLE
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        // Process for Notifications
        processNotification(intent)

        // Process for Deep Link
        processDeepLink(intent)
    }

    private fun processNotification(intent: Intent?) {
        authVM.notify = null
        val bundle = intent?.extras?.takeIf { !it.isEmpty } ?: return
        authVM.notify = bundle.getParcelable<NotificationActivityModel>("plans_notification")?.takeIf{ !it.notificationType.isNullOrEmpty() }
    }

    private fun processDeepLink(intent: Intent?) {
        if (intent == null) return
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                UserInfo.addShareContent(pendingDynamicLinkData?.link)
            }
            .addOnFailureListener(this) { e ->
                Log.d("MainActivity", "getDynamicLink:onFailure", e)
            }
    }



    private fun gotoFirstScreen() {
        intent.getIntExtra("resIdStartDestination", 0).also {
           if (it != 0) {
               mNavController?.graph?.also { navGraph ->
                   navGraph.startDestination = it
                   mNavController?.graph = navGraph
               }
           }else {
                lifecycleScope.launch (Dispatchers.IO){
                    delay(1500)
                    withContext(Dispatchers.Main) {
                        if (UserInfo.userId.isNullOrEmpty()){
                            PLANS_APP.currentFragment?.navigate(R.id.action_splashFragment_to_tutorialsFragment)
                        }else {
                            PLANS_APP.gotoDashboardActivity(notification = authVM.notify)
                            authVM.notify = null
                        }
                    }
                }
           }
        }
    }

    override fun onGlobalLayout() {
        binding.root.apply {
            if (measuredWidth > 0 && measuredHeight > 0) {
                OSHelper.widthScreen = measuredWidth
                OSHelper.heightScreen = measuredHeight
                viewTreeObserver.removeOnGlobalLayoutListener(this@MainActivity)
            }
        }
    }

}
