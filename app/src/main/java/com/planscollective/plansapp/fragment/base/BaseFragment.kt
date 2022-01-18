package com.planscollective.plansapp.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.extension.isKeyboardOpen
import com.planscollective.plansapp.extension.setLayoutHeight
import com.planscollective.plansapp.helper.KeyboardHelper
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.manager.AnalyticsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext




abstract class BaseFragment<VBinding : ViewBinding> : Fragment() {

    var _binding : VBinding? = null
    val binding get() = _binding!!

    lateinit var mNavController: NavController
    lateinit var mCallbackBackPressed : OnBackPressedCallback

    var curBackStackEntry: NavBackStackEntry? = null
    var preBackStackEntry: NavBackStackEntry? = null
    var viewStatusBar: View? = null
    open var screenName: String? = null

    // 1 - Created (Fragment)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // 2 - Created (Fragment) : Initialized (View)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    // 3 - Created (Fragment): Initialized (View)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mNavController = findNavController()
        curBackStackEntry = mNavController.currentBackStackEntry
        preBackStackEntry = mNavController.previousBackStackEntry

        initialize(view, savedInstanceState)
    }

    // 4 - Created (Fragment) : Created (View)
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    // 5 - Started
    override fun onStart() {
        super.onStart()
        PLANS_APP.currentFragment = this
    }

    // 6 - Resumed
    override fun onResume() {
        super.onResume()
        PLANS_APP.currentFragment = this
        AnalyticsManager.logScreenView(screenName, this.javaClass.kotlin.simpleName)
    }

    // 7 - Paused
    override fun onPause() {
        super.onPause()
    }

    // 8 - Stopped
    override fun onStop() {
        super.onStop()
    }

    // 9 - Stopped
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    // 10 - Stopped (Fragment) : Destroyed (View)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 11 - Destroyed
    override fun onDestroy() {
        super.onDestroy()
    }


    open fun initialize(view: View, savedInstanceState: Bundle?) {
        PLANS_APP.currentFragment = this

        initializeData()
        setupUI()
    }

    open fun initializeData(){

    }

    open fun setupUI(){
        // Status Bar View
        _binding?.root?.findViewById<View>(R.id.statusBar)?.apply {
            viewStatusBar = this
            setLayoutHeight(OSHelper.statusBarHeight)
        }
    }

    fun showKeyboard(editText: EditText) {
        KeyboardHelper.showKeyboard(this.requireActivity(), editText)
    }

    fun hideKeyboard() {
        KeyboardHelper.hideKeyboard(this.requireActivity())
    }

    open fun refreshAll(isShownLoading: Boolean = true, isUpdateLocation: Boolean = false): Boolean {
        return PLANS_APP.checkForBack()
    }

    open fun getNextPage(isShownLoading: Boolean = false) {

    }

    open fun gotoBack(destinationId: Int? = null, inclusive: Boolean = false) {
        val action = {
            try {
                if (destinationId != null) {
                    mNavController.popBackStack(destinationId, inclusive)
                }else {
                    mNavController.popBackStack()
                }
            }catch (e: Exception) {

            }
        }

        if (requireActivity().isKeyboardOpen()) {
            hideKeyboard()
            lifecycleScope.launch(Dispatchers.IO) {
                delay(100)
                withContext(Dispatchers.Main) {
                    action()
                }
            }
        }else {
            action()
        }
    }

    open fun navigate(resId: Int? = null, directions: NavDirections? = null) {
        val action = {
            try {
                if (resId != null) {
                    PLANS_APP.previousFragment = this
                    mNavController.navigate(resId)
                }else if (directions != null) {
                    PLANS_APP.previousFragment = this
                    mNavController.navigate(directions)
                }
            }catch (e : Exception) {

            }
        }

        if (requireActivity().isKeyboardOpen()) {
            hideKeyboard()
            lifecycleScope.launch(Dispatchers.IO) {
                delay(100)
                withContext(Dispatchers.Main) {
                    action()
                }
            }
        }else {
            action()
        }
    }

    open fun saveInfo(eventId: String? = null, userId: String? = null) {
        eventId?.takeIf { it.isNotEmpty() }?.also {
            curBackStackEntry?.savedStateHandle?.set("eventId", it)
        }

        userId?.takeIf { it.isNotEmpty() }?.also {
            curBackStackEntry?.savedStateHandle?.set("userId", it)
        }
    }

}