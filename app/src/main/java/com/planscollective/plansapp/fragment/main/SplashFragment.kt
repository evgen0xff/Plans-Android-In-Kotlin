package com.planscollective.plansapp.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.databinding.FragmentSplashBinding
import com.planscollective.plansapp.fragment.base.BaseFragment

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
    }

}