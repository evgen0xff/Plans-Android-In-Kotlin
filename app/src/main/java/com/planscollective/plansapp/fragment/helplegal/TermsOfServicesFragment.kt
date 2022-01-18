package com.planscollective.plansapp.fragment.helplegal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.databinding.FragmentHelplegalTermsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment

class TermsOfServicesFragment : AuthBaseFragment<FragmentHelplegalTermsBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHelplegalTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back Button
        binding.btnBack.setOnSingleClickListener {
            gotoBack()
        }
    }


}