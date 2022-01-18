package com.planscollective.plansapp.helper

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.extension.getRootView
import com.planscollective.plansapp.extension.isKeyboardOpen


interface OnKeyboardListener {
    fun onShownKeyboard() {}
    fun onHiddenKeyboard() {}
}

object KeyboardHelper {

    enum class KeyboardStatus {
        OPEN, CLOSED
    }

    private var keyboardStatus = MutableLiveData<KeyboardStatus>()
    private var listener: OnKeyboardListener? = null
    private var viewTreeObserver: ViewTreeObserver? = null
    private var lifecycleOwner: LifecycleOwner? = null

    private val observer = Observer<KeyboardStatus> {
        when(it) {
            KeyboardStatus.OPEN -> {
                listener?.onShownKeyboard()
            }
            KeyboardStatus.CLOSED -> {
                listener?.onHiddenKeyboard()
            }
            else -> {}
        }
    }

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun removeLayoutListener() {
            viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val status = if (PLANS_APP.currentActivity?.isKeyboardOpen() == true) {
            KeyboardStatus.OPEN
        } else {
            KeyboardStatus.CLOSED
        }
        if (status != keyboardStatus.value) {
            keyboardStatus.value = status
        }
    }




    fun showKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun showKeyboard(activity: Activity, edit: EditText) {
        val imm: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)
    }

    fun hideKeyboard(activity: Activity): Boolean {
        val view = activity.currentFocus
        if (view != null) {
            val imm =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            return true
        }
        return false
    }

    fun listenKeyboardEvent(fragment: Fragment?, keyboardListener: OnKeyboardListener? = null) {
        val activity = fragment?.requireActivity() ?: return
        val rootView = activity.getRootView()
        listener = keyboardListener

        viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver = rootView.viewTreeObserver
        viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)

        keyboardStatus.removeObserver(observer)
        lifecycleOwner?.also {
            it.lifecycle.removeObserver(lifecycleObserver)
        }

        lifecycleOwner = fragment.viewLifecycleOwner

        lifecycleOwner?.lifecycle?.addObserver(lifecycleObserver)
        lifecycleOwner?.also {
            keyboardStatus.observe(it, observer)
        }
    }



}

