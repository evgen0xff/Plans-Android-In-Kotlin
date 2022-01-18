package com.planscollective.plansapp.viewholders.user

import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.CellUserProfileBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.EventType
import com.planscollective.plansapp.viewholders.BaseViewHolder

class UserProfileVH(
    var itemBinding: CellUserProfileBinding,
    var listener: PlansActionListener? = null,
    var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null,
    var holderType: HolderType = HolderType.MINE
) : BaseViewHolder<UserModel>(itemBinding.root) {

    enum class HolderType {
        MINE,
        OTHERS
    }

    private var user: UserModel? = null
    private var typeEvent = EventType.ORGANIZED
    private var arrayTabTitles = ArrayList<TextView>()
    private var arrayTabBottoms = ArrayList<View>()

    init {
        itemBinding.apply {
            statusBar.setLayoutHeight(OSHelper.statusBarHeight)
            btnBack.visibility = if (holderType == HolderType.MINE) {
                btnMore.visibility = View.GONE
                btnSettings.visibility = View.VISIBLE
                btnActionFriends.visibility = View.GONE
                layoutSaved.visibility = View.VISIBLE
                View.GONE
            }else {
                btnMore.visibility = View.VISIBLE
                btnSettings.visibility = View.GONE
                btnActionFriends.visibility = View.VISIBLE
                layoutSaved.visibility = View.GONE
                View.VISIBLE
            }
            btnBack.setOnSingleClickListener {
                listener?.onClickedBack()
            }
            btnMore.setOnSingleClickListener {
                listener?.onClickedMoreMenuUser(user)
            }
            btnSettings.setOnSingleClickListener {
                listener?.onClickedSettings(user)
            }
            imvUserImage.setOnSingleClickListener {
                listener?.onClickedUserImage(user)
            }
            layoutStar.setOnSingleClickListener{
                listener?.onClickedCoinStar(user)
            }
            layoutFriends.setOnSingleClickListener {
                if (holderType == HolderType.MINE)
                    UserInfo.isSeenGuideFriends = true

                listener?.onClickedFriends(user)
            }
            imvGuideFriends.setOnSingleClickListener {
                UserInfo.isSeenGuideFriends = true
                listener?.onClickedFriends(user)
            }
            btnActionFriends.setOnSingleClickListener{
                listener?.onClickedFriendAction(user, tvActionFriends.text.toString())
            }
            layoutOrganized.setOnSingleClickListener{
                listener?.onClickedEventsTab(EventType.ORGANIZED, user)
            }
            layoutAttending.setOnSingleClickListener{
                listener?.onClickedEventsTab(EventType.ATTENDING, user)
            }
            layoutSaved.setOnSingleClickListener{
                listener?.onClickedEventsTab(EventType.SAVED, user)
            }

            arrayTabTitles.addAll(arrayOf(tvOrganized, tvAttending, tvSaved))
            arrayTabBottoms.addAll(arrayOf(bottomOrganized, bottomAttending, bottomSaved))
        }
        itemBinding.root.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    override fun bind(item: UserModel?, data: Any?, isLast: Boolean) {
        user = item
        (data as? EventType)?.also{ typeEvent = it }

        updateUserInfo()
        updateFriendShipUI()
        updateTabItems(typeEvent)
    }

    private fun updateUserInfo() {
        itemBinding.apply {
            // User Image
            imvUserImage.setUserImage(user?.profileImage)

            // Coin Star
            layoutStar.visibility = user?.coinNumber?.takeIf { it > 0 && it <= Constants.arrayStars.size }?.let{
                imvStarImage.setImageResource(Constants.arrayStars[it - 1])
                tvStarNumber.text = user?.getCountLiveEvents()
                View.VISIBLE
            } ?: View.GONE

            // User Name
            tvUserName.text = user?.fullName ?: user?.name ?: ((user?.firstName ?: "") + " " + (user?.lastName ?: ""))

            // User Location
            tvUserLocation.visibility = user?.userLocation?.takeIf { it.isNotEmpty() }?.let{
                tvUserLocation.text = it.removeOwnCountry()
                View.VISIBLE
            } ?: View.GONE

            // Events count
            tvEvents.text = user?.eventCount?.takeIf { it >= 0 }?.let { "$it"} ?: "0"

            // Friends count
            tvFriends.text = user?.friendsCount?.takeIf { it >= 0 }?.let { "$it"} ?: "0"

            // User About
            tvUserAbout.visibility = user?.bio?.takeIf{ it.isNotEmpty() }?.let {
                tvUserAbout.text = it
                View.VISIBLE
            } ?: View.GONE

            // Guide for Friends
            imvGuideFriends.visibility = if (holderType == HolderType.MINE && !UserInfo.isSeenGuideFriends){
                if (tvUserAbout.visibility == View.GONE && btnActionFriends.visibility == View.GONE ) {
                    layoutTabs.setLayoutMargin(top = 10.toPx())
                }
                View.VISIBLE
            } else View.GONE
        }
    }

    private fun updateFriendShipUI() {
        if (holderType == HolderType.MINE) return

        var text: String? = null
        var resId = R.drawable.bkgnd_purple_corner10

        user?.friendShipStatus?.also {
            when(it) {
                0 -> {
                    user?.friendRequestSender?.takeIf{ it1 -> it1 == UserInfo.userId }?.also {
                        text = ConstantTexts.REQUESTED
                        resId = R.drawable.bkgnd_teal_corner10
                    } ?: run {
                        text = ConstantTexts.CONFIRM_REQUEST
                        resId = R.drawable.bkgnd_purple_corner10
                    }
                }
                1 -> {
                    text = ConstantTexts.FRIENDS
                    resId = R.drawable.bkgnd_pink_corner10
                }
                5 -> {
                    text = ConstantTexts.UNBLOCK
                    resId = R.drawable.bkgnd_gray_dark_corner10
                }
                10 -> {
                    text = ConstantTexts.ADD_FRIEND
                    resId = R.drawable.bkgnd_purple_corner10
                }
            }
        }

        itemBinding.apply {
            tvActionFriends.text = text
            btnActionFriends.background = ContextCompat.getDrawable(PLANS_APP, resId)
            (user?.getAccessForMe()?.first ?: false).also {
                layoutFriends.isClickable = it
                layoutStar.isClickable = it
                layoutTabs.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateTabItems(type: EventType?) {
        val tab = type ?: EventType.ORGANIZED
        for (i in 0 until arrayTabTitles.size) {
            val tvTitle = arrayTabTitles[i]
            if (i == tab.value) {
                tvTitle.setTextColor(PlansColor.PURPLE_JOIN)
            }else {
                tvTitle.setTextColor(PlansColor.BLACK)
            }
        }

        for (i in 0 until arrayTabBottoms.size) {
            val viewBottom = arrayTabBottoms[i]
            if (i == tab.value) {
                viewBottom.setBackgroundColor(PlansColor.PURPLE_JOIN)
            }else {
                viewBottom.setBackgroundColor(Color.WHITE)
            }
        }
    }
}

