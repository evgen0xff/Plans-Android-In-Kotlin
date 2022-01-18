package com.planscollective.plansapp.fragment.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.CallbackManager
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.databinding.FragmentLandingBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.manager.FacebookLoginListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel


class LandingFragment : AuthBaseFragment<FragmentLandingBinding>(), FacebookLoginListener {

    lateinit var callbackManager : CallbackManager
    private var exoPlayer: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initializeData() {
        super.initializeData()
        authVM.setUser(UserModel())
        authVM.isSkipMode = false

        callbackManager = CallbackManager.Factory.create()
    }

    override fun setupUI() {
        super.setupUI()

        authVM.setupFBLogin(binding.btnFacebook, callbackManager, this, this)
        binding.apply {
            layoutFacebook.setOnSingleClickListener {
                btnFacebook.performClick()
            }

            tvLoginWithEmail.setOnSingleClickListener {
                navigate(R.id.action_landingFragment_to_nav_auth)
            }

            tvCreateAccount.setOnSingleClickListener {
                navigate(R.id.action_landingFragment_to_nav_signup)
            }
        }

        setupVideo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onDestroyView() {
        releaseMediaPlayer()
        super.onDestroyView()
    }

    fun setupVideo() {
        setMediaPlayer()
        val uriVideo = Uri.parse("android.resource://" + (activity?.packageName ?: "") + "/" + R.raw.vi_landing)
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uriVideo))
            prepare()
        }
    }

    private fun setMediaPlayer() {
        releaseMediaPlayer()
        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
        binding.playerView.apply {
            player = exoPlayer
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    private fun releaseMediaPlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        binding.playerView.player = null
    }


    override fun onLoggedInFB(result: LoginResult?) {
    }

    override fun onLoggedInPlans(user: UserModel?) {
        authVM.setUser(user)
        UserInfo.initForLogin(user)
        AnalyticsManager.logEvent(AnalyticsManager.EventType.LOGIN, UserInfo.userId)
        PLANS_APP.gotoDashboardActivity(user, ConstantTexts.SIGN_IN_SUCCESS)
    }

    override fun onCreatingPlansUser(user: UserModel?) {
        authVM.setUser(user)
        authVM.isSkipMode = true
        PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
    }

    override fun onCancel() {

    }

    override fun onFailed(error: FacebookException?) {

    }
}