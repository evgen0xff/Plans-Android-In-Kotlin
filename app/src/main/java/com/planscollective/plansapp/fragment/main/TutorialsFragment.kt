package com.planscollective.plansapp.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentTutorialsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment

class TutorialsFragment : AuthBaseFragment<FragmentTutorialsBinding>() {

    val listGuide = arrayOf(
        GuideFragment.GuidType.ORGANIZE,
        GuideFragment.GuidType.ATTEND,
        GuideFragment.GuidType.SHARE
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTutorialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.pager.adapter = PagerAdapter(this)
        binding.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.pager.offscreenPageLimit = listGuide.size

        binding.indicator.setViewPager(binding.pager)
        binding.indicator.createIndicators(listGuide.size, 0)

        binding.pager.registerOnPageChangeCallback(object:ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.indicator.animatePageSelected(position)
                updateUI()
            }
        })

        binding.tvGetStarted.setOnSingleClickListener {
            navigate(R.id.action_tutorialsFragment_to_landingFragment)
        }
    }

    private fun updateUI() {
        binding.tvGetStarted.isVisible = binding.pager.currentItem == listGuide.size - 1
    }


    inner class PagerAdapter : FragmentStateAdapter {
        constructor(fragment: Fragment) : super(fragment)

        override fun getItemCount() = listGuide.size

        override fun createFragment(position: Int): Fragment {
            return GuideFragment(listGuide[position])
        }
    }


}