package com.planscollective.plansapp.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.EventDetailsAdapter
import com.planscollective.plansapp.databinding.FragmentEventDetailsBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.ImageHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.EventDetailsVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDetailsFragment : PlansBaseFragment<FragmentEventDetailsBinding>(), MediaPickerListener{

    private val viewModel : EventDetailsVM by viewModels()
    private var adapterEventDetails: EventDetailsAdapter = EventDetailsAdapter()
    private val args: EventDetailsFragmentArgs by navArgs()
    private var layoutManager: LinearLayoutManager? = null
    override var screenName: String? = "Event_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        viewModel.eventId = args.eventId
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
        viewModel.messagePosting.observe(viewLifecycleOwner, Observer {
            updateSendUI()
        })
        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Cover View for Loading
        binding.viewCoverLoading.setLayoutHeight((OSHelper.widthScreen * 2 / 3.0).toInt())

        // Event List Recycler View
        layoutManager = object: LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                for (i in 0 until childCount) {
                    getChildAt(i)?.also {
                        viewModel.childSizesMap[getPosition(it)] = it.height
                    }
                }
            }

            override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
                if (childCount == 0) {
                    return 0
                }
                var scrolledY: Int = 0
                getChildAt(0)?.also {
                    val firstChildPosition = getPosition(it)
                    scrolledY = -it.y.toInt()
                    for (i in 0 until firstChildPosition) {
                        scrolledY += viewModel.childSizesMap[i] ?: 0
                    }
                }
                return scrolledY
            }
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapterEventDetails
        adapterEventDetails.listener = this

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val offset = recyclerView.computeVerticalScrollOffset().toDp()
                binding.layoutTopBar.visibility = if (offset > 100) {
                    View.VISIBLE
                }else {
                    View.GONE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        updatePostingUI()
                        adjustUIForScrolling()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        hideKeyboard()
                        updatePostingUI(true)
                    }
                }

            }
        })

        // Top Bar
        binding.layoutTopBar.visibility = View.GONE

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Menu Button
        binding.btnMenu.setOnSingleClickListener(this)

        // Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
        }

        // Posting UI
        binding.btnAttach.setOnSingleClickListener(this)
        binding.btnSend.setOnSingleClickListener(this)
        binding.btnCloseMedia.setOnSingleClickListener(this)
        binding.layoutPostingMedia.setOnSingleClickListener(this)
        binding.etMessage.addTextChangedListener {
            updateGuideView(true)
        }
        binding.imvGuideAddEventPostsNow.setOnSingleClickListener(this)

        updatePostingUI(true)

        // Loading Data from server
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

    private fun updateUI() {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)
        binding.viewModel = viewModel

        if (viewModel.eventModel?.isEnableToShow(UserInfo.userId) == false) {
            gotoBack()
        }else {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            adapterEventDetails.updateAdapter(viewModel.eventModel, viewModel.listPosts)

            updatePostingUI()
        }

        if (viewModel.eventModel?.isViewed == false && UserInfo.userId == viewModel.eventModel?.userId) {
            viewModel.eventModel?.isViewed = true
            gotoInviteByLink(viewModel.eventModel)
        }
    }

    private fun updatePostingUI(isHidden: Boolean = false) {
        _binding?.apply {
            // Posting Media layout
            layoutPostingMedia.visibility = if (viewModel?.urlPhotoPosting.isNullOrEmpty() && viewModel?.urlVideoPosting.isNullOrEmpty()) View.GONE else View.VISIBLE
            viewModel?.urlPhotoPosting?.also {
                imgvMedia.setEventImage(it)
            }
            viewModel?.bmpVideoThumb?.also {
                imgvMedia.setImageBitmap(it)
            }
            imgvPlay.visibility = if (viewModel?.urlVideoPosting.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Posting Message layout
            imgvUserImage.setUserImage(UserInfo.profileUrl)

            updateSendUI()

            layoutPosting.visibility = if (viewModel?.eventModel?.isPostingForMe() == true) {
                if (isHidden) View.GONE else View.VISIBLE
            }else {
                View.GONE
            }

            if (layoutPosting.visibility == View.GONE) {
                hideKeyboard()
            }
        }

        updateGuideView()
    }

    private fun updateGuideView(isSeen: Boolean? = null) {
        _binding?.apply {
            isSeen?.also {
                UserInfo.isSeenGuideAddEventPostNow = it
            }
            imvGuideAddEventPostsNow.visibility = if (!UserInfo.isSeenGuideAddEventPostNow && layoutPosting.visibility == View.VISIBLE) {
                val marginLeft = ((OSHelper.widthScreen / 2f) - 96.toPx()).toInt()
                imvGuideAddEventPostsNow.setLayoutMargin(left = marginLeft)
                View.VISIBLE
            }else View.GONE
        }
    }

    private fun updateSendUI() {
        binding.apply {
            if (!viewModel?.urlPhotoPosting.isNullOrEmpty() ||
                !viewModel?.urlVideoPosting.isNullOrEmpty() ||
                !viewModel?.messagePosting?.value.isNullOrEmpty() ) {
                btnSend.setImageResource(R.drawable.ic_send_purple)
                btnSend.isClickable = true
            }else {
                btnSend.setImageResource(R.drawable.ic_send_grey)
                btnSend.isClickable = false
            }
        }
    }

    private fun adjustUIForScrolling() {
        binding.apply {
            val first = recyclerView.adapter?.itemCount?.takeIf{ it > 0}?.let{0}
            val firstVisible = layoutManager?.findFirstCompletelyVisibleItemPosition()
            val last = recyclerView.adapter?.itemCount?.takeIf{ it > 0}?.let{ it - 1 }
            val lastVisible = layoutManager?.findLastCompletelyVisibleItemPosition()
            lifecycleScope.launch(Dispatchers.IO) {
                delay(50)
                withContext(Dispatchers.Main) {
                    val position = if (lastVisible == last && firstVisible != first) {
                        last
                    }else if (firstVisible == first && lastVisible != last  ) {
                        first
                    }else -1

                    position?.takeIf { it >= 0 }?.also {
                        recyclerView.smoothScrollToPosition(it)
                    }
                }
            }
        }
    }

    private fun sendPost() {
        hideKeyboard()
        viewModel.createPost()
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.btnMenu -> {
                MenuOptionHelper.showPlansMenu(viewModel.eventModel, MenuOptionHelper.MenuType.EVENT_DETAILS, this, this)
            }
            binding.btnAttach -> {
                updateGuideView(true)
                MediaPickerHelper.showPickerOptions(this, this)
            }
            binding.btnCloseMedia -> {
                viewModel.urlVideoPosting = null
                viewModel.urlPhotoPosting = null
                updatePostingUI()
            }
            binding.layoutPostingMedia -> {
                if (!viewModel.urlVideoPosting.isNullOrEmpty()) {
                    gotoPlayVideo(viewModel.urlVideoPosting)
                }else if (!viewModel.urlPhotoPosting.isNullOrEmpty()) {
                    gotoOpenPhoto(viewModel.urlPhotoPosting)
                }
            }
            binding.btnSend -> {
                sendPost()
            }
            binding.imvGuideAddEventPostsNow -> {
                updateGuideView(true)
            }
        }
    }

    //********************************* MediaPickerListener **************************************//
    override fun onSelectedPhoto(filePath: String?) {
        viewModel.urlPhotoPosting = filePath
        viewModel.urlVideoPosting = null
        viewModel.bmpVideoThumb = null
        updatePostingUI()
    }

    override fun onSelectedVideo(filePath: String?) {
        viewModel.bmpVideoThumb = ImageHelper.getVideoThumbnail(filePath)
        binding.imgvMedia.setImageBitmap(viewModel.bmpVideoThumb)
        viewModel.urlVideoPosting = filePath
        viewModel.urlPhotoPosting = null
        updatePostingUI()
    }

}