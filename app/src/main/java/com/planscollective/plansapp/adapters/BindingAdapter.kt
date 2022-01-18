package com.planscollective.plansapp.adapters

import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter

@BindingAdapter("onEnterKeyListener")
fun setOnEnterKeyListener(editText: EditText, action: ((view: View) -> Unit)? = null) {
    editText.setOnKeyListener { view, keyCode, event ->
        if (event?.action == KeyEvent.ACTION_DOWN &&
            keyCode == KeyEvent.KEYCODE_ENTER
        ) {
            action?.let{it(view)}
            true
        } else {
            false
        }
    }
}
