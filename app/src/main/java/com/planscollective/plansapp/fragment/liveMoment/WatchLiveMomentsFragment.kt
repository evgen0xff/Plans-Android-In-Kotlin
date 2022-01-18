package com.planscollective.plansapp.fragment.liveMoment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.customUI.ReadMoreTextView
import com.planscollective.plansapp.customUI.segmentedProgressView.SegmentedProgressBarListener
import com.planscollective.plansapp.databinding.FragmentWatchLiveMomentBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.OnSwipeTouchListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.LiveMomentModel
import com.planscollective.plansapp.models.dataModels.UserLiveMomentsModel
import com.planscollective.plansapp.models.viewModels.WatchLiveMomentsVM
import com.google.android.exoplayer2.ExoPlayer
import com.planscollective.plansapp.webServices.videoCache.VideoCacheService


class WatchLiveMomentsFragment : PlansBaseFragment<FragmentWatchLiveMomentBinding>(),
    SegmentedProgressBarListener,
    ReadMoreTextView.OnReadMoreListener,
    Player.Listener
{

    private val viewModel: WatchLiveMomentsVM by viewModels()
    private val args: WatchLiveMomentsFragmentArgs by navArgs()
    private var exoPlayer: ExoPlayer? = null
    private var isExitToDown = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWatchLiveMomentBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            updateUI(it)
        })
        viewModel.curStatus.observe(viewLifecycleOwner, {
            updateUIStatus(it)
        })
        viewModel.eventId = args.eventId
        viewModel.userId = args.userId
        viewModel.liveMomentId = args.liveMomentId

        binding.viewModel = viewModel

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupUI() {
        super.setupUI()
        // Top bar
        binding.layoutUser.setOnSingleClickListener(this)
        binding.btnMenu.setOnSingleClickListener(this)
        binding.btnChat.setOnSingleClickListener(this)
        binding.btnClose.setOnSingleClickListener(this)

        // Video View
        setMediaPlayer()

        // Progress View
        binding.progressView.listener = this

        // Gesture View
        binding.layoutGesture.setOnTouchListener(object : OnSwipeTouchListener(requireContext()){
            override fun onSwipeDown() {
                isExitToDown = true
                gotoBack()
            }
            override fun onSwipeLeft() {
                nextLiveMoment()
            }
            override fun onSwipeRight() {
                previousLiveMoment()
            }
            override fun onLongPressed() {
                pauseLiveMoment()
            }
            override fun onTouchUp() : Boolean{
                resumeLiveMoment()
                return true
            }
            override fun onClicked(e: MotionEvent?): Boolean {
                e?.x?.also {
                    if (it > (OSHelper.widthScreen * 2 / 3.0)) {
                        nextLiveMoment()
                    }else if (it > (OSHelper.widthScreen / 3.0)){
                        resumeLiveMoment()
                    }else {
                        previousLiveMoment()
                    }
                }
                return true
            }
        })


        // Tutorial View
        binding.layoutTutorial.setOnSingleClickListener(this)
        binding.layoutTutorial.visibility = if (UserInfo.isSeenGuideWatchMoment) View.GONE else View.VISIBLE

        // Caption View
        binding.tvCaption.setOnReadMoreListener(this)

        if (viewModel.curStatus.value == null) {
            viewModel.curStatus.value = WatchLiveMomentsVM.Status.NONE
        }

        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    override fun getNextPage(isShownLoading: Boolean) {
        super.getNextPage(isShownLoading)
        viewModel.getNextPage(requireContext(), isShownLoading)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (!enter && isExitToDown ) {
            return AnimationUtils.loadAnimation(requireContext(), R.anim.move_out_down)
        }else {
            return super.onCreateAnimation(transit, enter, nextAnim)
        }
    }


    private fun updateUI(isDidLoaded: Boolean = true) {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)

        if (!isDidLoaded) {
            gotoBack()
            return
        }

        // Progress View
        updateProgressView()

        // Play Live Moments
        playLiveMoments(viewModel.curIndex, !UserInfo.isSeenGuideWatchMoment)
    }

    private fun updateProgressView() {
        binding.apply {
            progressView.segmentCount = viewModel?.listLiveMoments?.size ?: 0
        }
    }

    private fun playLiveMoments(index: Int = 0, isPause: Boolean = false) {
        viewModel.prepareData(index)
        prepareUI(viewModel.curUserMomentModel, isPause)
    }

    private fun prepareUI(userLiveMoments: UserLiveMomentsModel?, isPause: Boolean = false) {
        binding.apply {
            // Progress View
            progressView.timePerSegmentMs = Constants.DURATION_PLAY_PHOTO

            if (!isPause) {
                progressView.start()
            }

            // User Image
            imvUser.setUserImage(userLiveMoments?.user?.profileImage)

            // User Name
            tvUserName.text = userLiveMoments?.user?.fullName

            // Ago Time
            tvAgoTime.text = viewModel?.curLiveMoment?.createdAt?.toLocalDateTime()?.timeAgoSince()

            // Caption
            updateCaptionText(viewModel?.curLiveMoment?.media, ViewGroup.LayoutParams.WRAP_CONTENT, true)

            // Media
            updateMedia(isPause)
        }

    }

    private fun updateUIStatus(status: WatchLiveMomentsVM.Status? = WatchLiveMomentsVM.Status.NONE) {
        binding.apply {
            progressView.visibility = View.VISIBLE
            layoutTopBar.visibility = View.VISIBLE
            srvCaption.visibility = if (viewModel?.curLiveMoment?.media.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

            btnMenu.visibility = View.VISIBLE
            btnChat.visibility = if (viewModel?.isMine == true) View.GONE else View.VISIBLE
            btnClose.visibility = View.VISIBLE

            when(status) {
                WatchLiveMomentsVM.Status.NONE -> {
                    progressView.visibility = View.INVISIBLE
                    layoutTopBar.visibility = View.INVISIBLE
                    imageView.visibility = View.INVISIBLE
                    containerVideo.visibility = View.INVISIBLE
                    srvCaption.visibility = View.INVISIBLE
                }
                WatchLiveMomentsVM.Status.PHOTO_PAUSE,
                WatchLiveMomentsVM.Status.PHOTO_PLAYING -> {
                    imageView.visibility = View.VISIBLE
                    containerVideo.visibility = View.INVISIBLE
                }
                WatchLiveMomentsVM.Status.VIDEO_PAUSE,
                WatchLiveMomentsVM.Status.VIDEO_PLAYING -> {
                    imageView.visibility = View.INVISIBLE
                    containerVideo.visibility = View.VISIBLE
                }
            }

            when(status) {
                WatchLiveMomentsVM.Status.NONE,
                WatchLiveMomentsVM.Status.PHOTO_PAUSE,
                WatchLiveMomentsVM.Status.VIDEO_PAUSE -> {
                    pauseLiveMoment()
                }
                WatchLiveMomentsVM.Status.PHOTO_PLAYING,
                WatchLiveMomentsVM.Status.VIDEO_PLAYING -> {
                    resumeLiveMoment()
                }
            }
        }
    }

    private fun updateCaptionText(text: String? = null, height: Int? = null, isCollapsed: Boolean? = null) {
        binding.apply {
            isCollapsed?.let {tvCaption.setIsCollapsed(it)}
            tvCaption.text = text ?: viewModel?.curLiveMoment?.media
            height?.let {srvCaption.setLayoutHeight(it)}
        }
    }

    private fun updateMedia(isPause: Boolean = false) {
        val isPause1 = isPause
        viewModel.curLiveMoment?.imageOrVideo?.also {
            when(viewModel.curLiveMoment?.mediaType) {
                "video" -> {
                    binding.imvThumb.setImage(viewModel.curLiveMoment?.liveThumbnail)
                    val proxyUrl = VideoCacheService.getProxyUrl(it)
                    val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(proxyUrl))
                    exoPlayer?.apply {
                        setMediaItem(mediaItem)
                        prepare()
                        if (!isPause) {
                            BusyHelper.show(requireContext())
                            play()
                        }
                        viewModel.curStatus.value = WatchLiveMomentsVM.Status.VIDEO_PAUSE
                    }
                }
                "image" -> {
                    BusyHelper.show(requireContext())
                    viewModel.curStatus.value = WatchLiveMomentsVM.Status.PHOTO_PAUSE
                    binding.imageView.setImage(it){
                        BusyHelper.hide()
                        if (!isPause1) {
                            viewModel.curStatus.value = WatchLiveMomentsVM.Status.PHOTO_PLAYING
                        }
                        viewModel?.viewedLiveMoment()
                    }
                }
                else -> {}
            }
        }
    }

    private fun setMediaPlayer() {
        releaseMediaPlayer()

        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
        exoPlayer?.addListener(this)
        binding.videoView.apply {
            player = exoPlayer
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    private fun nextLiveMoment() {
        binding.apply {
            if (viewModel?.curIndex == (progressView.segmentCount - 1)) {
                gotoBack()
            }else {
                progressView.next()
            }
        }
    }

    private fun previousLiveMoment() {
        binding.progressView.previous()
    }

    private fun pauseLiveMoment() {
        binding.progressView.pause()
        when(viewModel.curStatus.value) {
            WatchLiveMomentsVM.Status.VIDEO_PLAYING, WatchLiveMomentsVM.Status.VIDEO_PAUSE -> {
                exoPlayer?.pause()
            }
            else -> {}
        }
    }

    private fun resumeLiveMoment() {
        binding.progressView.start()
        when (viewModel.curStatus.value) {
            WatchLiveMomentsVM.Status.VIDEO_PLAYING, WatchLiveMomentsVM.Status.VIDEO_PAUSE -> {
                exoPlayer?.play()
            }
            else -> {}
        }
    }

    private fun releaseMediaPlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer?.removeListener(this)
        exoPlayer = null
        binding.videoView.player = null
    }

    override fun onDestroyView() {
        pauseLiveMoment()
        releaseMediaPlayer()
        super.onDestroyView()
    }

    override fun reportContent(content: Any?, complete: (() -> Unit)?) {
        super.reportContent(content) {
            resumeLiveMoment()
        }
    }

    override fun deleteLiveMoment(
        liveMoment: LiveMomentModel?,
        event: EventModel?,
        complete: (() -> Unit)?
    ) {
        super.deleteLiveMoment(liveMoment, event) {
            resumeLiveMoment()
        }
    }

    //******************************* View.OnClick listener *********************************//
    override fun onSingleClick(v: View?) {
        binding.let {
            when(v) {
                it.layoutUser -> {
                    pauseLiveMoment()
                    gotoUserProfile(viewModel.curUserMomentModel?.user)
                }
                it.btnMenu -> {
                    pauseLiveMoment()
                    val data = hashMapOf("content" to viewModel.curLiveMoment,
                        "event" to viewModel.eventModel,
                        "userLiveMoment" to viewModel.curUserMomentModel
                    )
                    MenuOptionHelper.showPlansMenu(data, MenuOptionHelper.MenuType.LIVE_MOMENT, this, this)
                }
                it.btnChat -> {
                    pauseLiveMoment()
                    gotoChatting(user = viewModel.curUserMomentModel?.user)
                }
                it.btnClose -> {
                    gotoBack()
                }
                it.layoutTutorial -> {
                    UserInfo.isSeenGuideWatchMoment = true
                    it.layoutTutorial.visibility = View.GONE
                    playLiveMoments(viewModel.curIndex ?: 0)
                    it.progressView.start()
                }
            }
        }
    }

    //********************************* ReadMoreTextView.OnReadMoreListener ************************************//
    override fun onExpanded(v: View?) {
        pauseLiveMoment()
        var height = (viewModel.curLiveMoment?.media?.getHeight(requireContext(),
            16,
            OSHelper.widthScreen - 32.toPx(),
            R.font.product_sans_regular) ?: 0) + 20.toPx()

        if (height > 20.toPx()) {
            height = if (height > 250.toPx()) {
                250.toPx()
            }else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
            updateCaptionText(height = height)
        }
    }

    override fun onCollapsed(v: View?) {
        pauseLiveMoment()
        updateCaptionText(height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    //********************************* Player.Listener ************************************//
    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        binding.apply {
            when(state) {
                Player.STATE_READY -> {
                    BusyHelper.hide()
                    progressView.timePerSegmentMs = exoPlayer?.duration ?: Constants.DURATION_PLAY_PHOTO
                    viewModel?.curStatus?.value = WatchLiveMomentsVM.Status.VIDEO_PLAYING
                    viewModel?.viewedLiveMoment()
                }
                Player.STATE_ENDED -> {
                }
                else -> {
                }
            }
        }
    }


    //********************************* SegmentedProgressBarListener ************************************//

    override fun onFinished() {
        gotoBack()
    }

    override fun onPage(oldPageIndex: Int, newPageIndex: Int) {
        viewModel.curIndex = newPageIndex.takeIf { it >= 0 } ?: return
        pauseLiveMoment()
        playLiveMoments(newPageIndex)
    }

}