package com.planscollective.plansapp.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentGuideBinding
import com.planscollective.plansapp.fragment.base.BaseFragment

class GuideFragment : BaseFragment<FragmentGuideBinding> {

    enum class GuidType {
        ORGANIZE,
        ATTEND,
        SHARE
    }

    var typeGuide = GuidType.ORGANIZE

    constructor(typeGuide: GuidType) : super() {
        this.typeGuide = typeGuide
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        updateUI(typeGuide)
    }

    private fun updateUI(typeGuide: GuidType) {
        this.typeGuide = typeGuide

        var title = R.string.guide_organize_title
        var description = R.string.guide_organize_description
        var image = R.drawable.im_tutorial_organize

        when(typeGuide) {
            GuidType.ORGANIZE -> {
                title = R.string.guide_organize_title
                description = R.string.guide_organize_description
                image = R.drawable.im_tutorial_organize
            }
            GuidType.ATTEND -> {
                title = R.string.guide_attend_title
                description = R.string.guide_attend_description
                image = R.drawable.im_tutorial_attend
            }
            GuidType.SHARE -> {
                title = R.string.guide_share_title
                description = R.string.guide_share_description
                image = R.drawable.im_tutorial_share
            }
        }

        binding.txtvTitle.setText(title)
        binding.txtvDescription.setText(description)
        binding.imageView.setImageResource(image)
    }
}