package com.planscollective.plansapp.customUI

import android.content.Context
import android.graphics.PointF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.otaliastudios.cameraview.markers.AutoFocusMarker
import com.otaliastudios.cameraview.markers.AutoFocusTrigger
import com.planscollective.plansapp.databinding.ViewLingBinding

class LingView : AutoFocusMarker {
    var viewLing: View? = null
    var binding: ViewLingBinding? = null

    override fun onAttach(context: Context, container: ViewGroup): View? {
        binding = ViewLingBinding.inflate(LayoutInflater.from(context), container, false)
        viewLing = binding?.viewLing
        binding?.root?.visibility = View.GONE
        return binding?.root
    }

    override fun onAutoFocusStart(trigger: AutoFocusTrigger, point: PointF) {
        if (trigger == AutoFocusTrigger.METHOD) return
        binding?.root?.visibility = View.VISIBLE
    }

    override fun onAutoFocusEnd(trigger: AutoFocusTrigger, successful: Boolean, point: PointF) {
        if (trigger == AutoFocusTrigger.METHOD) return
        binding?.root?.visibility = View.GONE
    }
}