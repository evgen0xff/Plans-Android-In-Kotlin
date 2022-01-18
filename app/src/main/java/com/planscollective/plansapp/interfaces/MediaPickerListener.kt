package com.planscollective.plansapp.interfaces

import androidx.fragment.app.FragmentActivity

interface MediaPickerListener {

    fun requireActivity() : FragmentActivity?

    fun onSelectedPhoto(filePath: String?)

    fun onSelectedVideo(filePath: String?)
}