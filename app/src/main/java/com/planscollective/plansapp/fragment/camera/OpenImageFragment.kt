package com.planscollective.plansapp.fragment.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.planscollective.plansapp.databinding.FragmentOpenImageBinding
import com.planscollective.plansapp.extension.setImage
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.BaseFragment

class OpenImageFragment : BaseFragment<FragmentOpenImageBinding>() {

    private val args : OpenImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOpenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        binding.btnClose.setOnSingleClickListener {
            gotoBack()
        }
        binding.tvTitle.text = args.title
        binding.imageView.setImage (args.urlPhoto)
    }

}