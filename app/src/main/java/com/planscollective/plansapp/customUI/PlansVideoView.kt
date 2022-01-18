package com.planscollective.plansapp.customUI

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.planscollective.plansapp.databinding.ViewPlansVideoBinding
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.webServices.videoCache.VideoCacheService


class PlansVideoView : RelativeLayout, Player.Listener {

    lateinit var binding: ViewPlansVideoBinding
    private var exoPlayer: ExoPlayer? = null
    private var thumbUrl: String? = null
    private var videoUrl: String? = null

    constructor(context: Context) : super(context) {
        onInit(context)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onInit(context)
    }

    private fun onInit(context: Context) {
        binding = ViewPlansVideoBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun playVideoUrl(urlVideo: String? = null,
                     urlThumbImage: String? = null,
                     complete: ((success: Boolean?) -> Unit)? = null
    ) {
        videoUrl = urlVideo ?: videoUrl
        thumbUrl = urlThumbImage ?: thumbUrl

        binding.imageView.setEventImage(thumbUrl, complete = complete)

        videoUrl?.takeIf{ it.isNotEmpty() }?.also {
            setMediaPlayer()
            // Build the media item.
            val proxyUrl = VideoCacheService.getProxyUrl(it)
            val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(proxyUrl))
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }

    private fun setMediaPlayer() {
        releaseMediaPlayer()
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
        exoPlayer?.addListener(this)
        binding.styledPlayerView.apply {
            player = exoPlayer
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    private fun releaseMediaPlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer?.removeListener(this)
        exoPlayer = null
        binding.styledPlayerView.player = null
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        binding.apply {
            when(state) {
                Player.STATE_READY -> {
                }
                else -> {
                    imageView.visibility = View.VISIBLE
                    imgvPlay.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            val imageView = binding.imageView
            val imgvPlay = binding.imgvPlay
            postDelayed({
                imageView.visibility = View.INVISIBLE
                imgvPlay.visibility = View.INVISIBLE
            }, 500)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        binding.apply {
            imgvPlay.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playVideoUrl()
    }

    override fun onDetachedFromWindow() {
        releaseMediaPlayer()
        super.onDetachedFromWindow()
    }

}