package com.planscollective.plansapp.viewholders.event

import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.databinding.CellEventCoverBinding
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.extension.setLayoutHeight
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder


class EventCoverViewHolder(
    private val itemBinding: CellEventCoverBinding,
    var listener: PlansActionListener? = null,
    var type: EventCoverType = EventCoverType.EVENT_FEED
) : EventBaseViewHolder(itemBinding.root), OnSingleClickListener, ViewTreeObserver.OnGlobalLayoutListener{

    enum class EventCoverType {
        EVENT_FEED,
        EVENT_DETAILS
    }

    init {
        itemView.setOnSingleClickListener(this)
        itemBinding.apply {
            statusBar.setLayoutHeight(OSHelper.statusBarHeight)
            layoutRoot.viewTreeObserver.addOnGlobalLayoutListener(this@EventCoverViewHolder)
        }
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        if (item != null) {
            itemBinding.apply {

                // Event Cover Image/Video
                layoutBottomBar.visibility = View.GONE
                if (item.mediaType == "video"){
                    videoView.visibility = View.VISIBLE
                    videoView.playVideoUrl(item.imageOrVideo, item.thumbnail){
                        layoutBottomBar.visibility = if (it == true) View.VISIBLE else View.GONE
                    }
                }else {
                    videoView.visibility = View.GONE
                    imgvEventCover.setEventImage(item.imageOrVideo){
                        layoutBottomBar.visibility = if (it == true) View.VISIBLE else View.GONE
                    }
                }

                // Event Status
                val (title, color) = item.getEventStatus()
                tvStartTime.setTextColor(color)
                tvStartTime.text = title
                imgvClock.setColorFilter(color)

                // Live Mark
                imgvLive.visibility = if (item.isLive == 1) View.VISIBLE else View.GONE
                if (imgvLive.visibility == View.VISIBLE) {
                    val anim = AnimationUtils.loadAnimation(imgvLive.context, R.anim.blinking_live)
                    imgvLive.startAnimation(anim)
                }else {
                    imgvLive.clearAnimation()
                }

                // Views Count
                val countViews = item.countViews ?: 0
                layoutViews.visibility = if (countViews > 0) View.VISIBLE else View.GONE
                tvEventViews.text = if (countViews > 1) "$countViews Views" else "$countViews View"

                // Posts Count
                val countPosts = item.countPosts ?: 0
                layoutPosts.visibility = if (countPosts > 0) View.VISIBLE else View.GONE
                tvEventPosts.text = if (countPosts > 1) "$countPosts Posts" else "$countPosts Post"

                // Friends Count
                val countFriends = item.getFriendsCount()
                layoutFriends.visibility = if (countFriends > 0) View.VISIBLE else View.GONE
                tvEventFriends.text = if (countFriends > 1) "$countFriends Friends" else "$countFriends Friend"
            }
            setupUI(type)
        }
    }



    override fun onSingleClick(v: View?) {
        itemView.apply {
            when(v) {
                this -> {
                    when (type) {
                        EventCoverType.EVENT_FEED -> {
                            listener?.onClickedEvent(eventModel)
                        }
                        EventCoverType.EVENT_DETAILS -> {
                            if (eventModel?.mediaType == "video") {
                                listener?.onClickPlayVideo(eventModel?.imageOrVideo, eventModel)
                            }else {
                                listener?.onClickOpenPhoto(eventModel?.imageOrVideo, eventModel)
                            }
                        }
                    }
                }
                itemBinding.btnBack -> {
                    listener?.onClickedBack()
                }
                itemBinding.btnMenu -> {
                    listener?.onClickedMoreMenu(eventModel,
                        if (type == EventCoverType.EVENT_FEED)
                        MenuOptionHelper.MenuType.EVENT_FEED else
                            MenuOptionHelper.MenuType.EVENT_DETAILS)
                }
            }
        }
    }

    override fun onGlobalLayout() {
        itemBinding.let {
            it.layoutRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)
            val width = it.layoutRoot.measuredWidth
            val height = if (type == EventCoverType.EVENT_FEED)  width / 1.77 else ((width * 2 / 3.0) + OSHelper.statusBarHeight)

            if(height > 0) {
                it.layoutRoot.setLayoutHeight(height.toInt())
            }
        }
    }


    private fun setupUI(type: EventCoverType) {
        itemBinding.let {
            when (type) {
                EventCoverType.EVENT_FEED -> {
                    it.statusBar.visibility = View.GONE
                    it.btnBack.visibility = View.GONE
                    it.btnMenu.visibility = View.GONE
                }
                EventCoverType.EVENT_DETAILS -> {
                    it.statusBar.visibility = View.VISIBLE
                    it.btnBack.visibility = View.VISIBLE
                    it.btnMenu.visibility = View.VISIBLE
                    it.layoutStatusTime.visibility = if (eventModel?.isEnded == 1) View.GONE else View.VISIBLE

                    it.btnBack.setOnSingleClickListener(this)
                    it.btnMenu.setOnSingleClickListener(this)
                }
            }
        }
    }

}