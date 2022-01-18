package com.planscollective.plansapp.fragment.camera

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.*
import com.planscollective.plansapp.databinding.FragmentVideoPlayerBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.viewModels.VideoPlayerVM
import com.planscollective.plansapp.webServices.videoCache.VideoCacheService

class VideoPlayerFragment: PlansBaseFragment<FragmentVideoPlayerBinding>(), Player.Listener{

    private val viewModel: VideoPlayerVM by viewModels()
    private val args: VideoPlayerFragmentArgs by navArgs()
    private var exoPlayer: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        viewModel.urlString = args.urlVideo
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.btnClose.setOnSingleClickListener {
            gotoBack()
        }
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        exoPlayer?.addListener(this)
        binding.playerView.player = exoPlayer

        exoPlayer?.apply {
            viewModel.urlString?.takeIf { it.isNotEmpty() }?.also {
                val proxyUrl = VideoCacheService.getProxyUrl(it)
                setMediaItem(MediaItem.fromUri(Uri.parse(proxyUrl)))
            }
            playWhenReady = viewModel.playWhenReady
            seekTo(viewModel.currentWindow, viewModel.playbackPosition)
            prepare()
        }
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }

    override fun onResume() {
        super.onResume()
        if (exoPlayer == null) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }


    private fun releasePlayer() {
        if (exoPlayer == null) {
            return
        }

        viewModel.playWhenReady = exoPlayer!!.playWhenReady
        viewModel.playbackPosition = exoPlayer!!.currentPosition
        viewModel.currentWindow = exoPlayer!!.currentWindowIndex

        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer?.removeListener(this)
        exoPlayer = null
        binding.playerView.player = null
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        binding.apply {
            when(state) {
                Player.STATE_READY -> {
                }
                Player.STATE_BUFFERING -> {
                }
                else -> {
                }
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
    }

}