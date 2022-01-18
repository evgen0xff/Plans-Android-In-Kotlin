package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.CellLiveMomentBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.LiveMomentModel
import com.planscollective.plansapp.models.dataModels.UserLiveMomentsModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class LiveMomentsAdapter(
    var event: EventModel? = null,
    var listLiveMoments: ArrayList<UserLiveMomentsModel> = ArrayList(),
    var listener: PlansActionListener? = null,
    var typeAdapter: AdapterType = AdapterType.EVENT_DETAILS
): RecyclerView.Adapter<LiveMomentsAdapter.LiveMomentViewHolder>() {

    enum class AdapterType {
        EVENT_DETAILS,
        FULL_LIVE_MOMENTS
    }

    companion object {
        const val TYPE_ADD_LIVE_MOMENT = 0
        const val TYPE_LIVE_MOMENT = 1
        const val TYPE_EMPTY    = 2
    }

    private var isEnableAdd = false
    private var countEmptyCells = 3
    private var listItems = ArrayList<HashMap<String, Any?>>()
    private var widthCell = 98.toPx()
    private var heightCell = 173.toPx()

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(eventModel: EventModel? = null, listMoments: ArrayList<UserLiveMomentsModel>? = null, listener: PlansActionListener? = null) {
        eventModel?.let {
            event = it
        }
        listener?.let {
            this.listener = it
        }
        val list = listMoments ?: eventModel?.liveMoments
        list?.let{
            listLiveMoments.clear()
            listLiveMoments.addAll(it)
        }

        isEnableAdd = event?.isEnableAddLiveMoment() ?: false

        when(typeAdapter) {
            AdapterType.EVENT_DETAILS -> {
                countEmptyCells = (if (isEnableAdd) 3 else 4) - listLiveMoments.size
                countEmptyCells = if (countEmptyCells > 0) countEmptyCells else 0
                widthCell = 98.toPx()
                heightCell = 173.toPx()
            }
            AdapterType.FULL_LIVE_MOMENTS -> {
                countEmptyCells = 0
                widthCell = ((OSHelper.widthScreen - (12 * 2).toPx()) / 3.0).toInt()
                heightCell = (widthCell.toFloat() * (173.0 / 98.0)).toInt()
            }
        }

        listItems.clear()

        if (isEnableAdd) {
            listItems.add(hashMapOf("type" to TYPE_ADD_LIVE_MOMENT,
                                    "data" to event))
        }

        listLiveMoments.forEach {
            listItems.add(hashMapOf("type" to TYPE_LIVE_MOMENT,
                "data" to it))
        }

        for(i in 0 until countEmptyCells) {
            listItems.add(hashMapOf("type" to TYPE_EMPTY,
                "data" to event))
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMomentViewHolder {
        val itemBinding = CellLiveMomentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LiveMomentViewHolder(itemBinding)

    }

    override fun onBindViewHolder(holder: LiveMomentViewHolder, position: Int) {
        val item = listItems[position]
        holder.bind(item["type"] as? Int, item["data"])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    inner class LiveMomentViewHolder (private val itemBinding: CellLiveMomentBinding): BaseViewHolder<Int>(itemBinding.root) {
        var type: Int? = null
        private var userLiveMomentsModel: UserLiveMomentsModel? = null
        private var lastLiveMoment: LiveMomentModel? = null

        init {
            itemView.setLayoutSize(widthCell, heightCell)
            itemView.setOnSingleClickListener {
                when(type) {
                    TYPE_ADD_LIVE_MOMENT -> {
                        UserInfo.isSeenGuidePosts = true
                        listener?.onClickAddLiveMoment(event)
                    }
                    TYPE_LIVE_MOMENT -> {
                        listener?.onClickWatchUserLiveMoments(userLiveMomentsModel, event)
                    }
                }
            }
        }

        override fun bind(item: Int?, data: Any?, isLast: Boolean) {
            type = item
            itemBinding.apply {
                when(type) {
                    TYPE_ADD_LIVE_MOMENT -> {
                        layoutAddLiveMoment.visibility = View.VISIBLE
                        layoutLiveMoment.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                        imgvUser.setUserImage(UserInfo.profileUrl)
                        if (typeAdapter == AdapterType.EVENT_DETAILS) {
                             tvAddLiveMoment.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13F)
                        }else {
                            tvAddLiveMoment.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17F)
                        }
                    }
                    TYPE_LIVE_MOMENT -> {
                        layoutAddLiveMoment.visibility = View.GONE
                        layoutLiveMoment.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE

                        userLiveMomentsModel = data as? UserLiveMomentsModel
                        lastLiveMoment = userLiveMomentsModel?.liveMoments?.firstOrNull()

                        // Live Moment Image
                        val urlString: String? = if (lastLiveMoment?.mediaType == "image") {
                            lastLiveMoment?.imageOrVideo
                        }else {
                            lastLiveMoment?.liveThumbnail
                        }
                        imgvLiveMoment.setEventImage(urlString)

                        // User Profile Image
                        imgvUserLiveMoment.setUserImage(userLiveMomentsModel?.user?.profileImage)

                        // User Name
                        tvUserName.text = if (userLiveMomentsModel?.user?.id == UserInfo.userId) {
                            val typeFace = ResourcesCompat.getFont(PLANS_APP, R.font.product_sans_bold)
                            tvUserName.typeface = typeFace
                            "Your\nLive Moments"
                        }else {
                            val typeFace = ResourcesCompat.getFont(PLANS_APP, R.font.product_sans_regular)
                            tvUserName.typeface = typeFace
                            userLiveMomentsModel?.user?.fullName
                        }

                        // Pink border background
                        if (userLiveMomentsModel?.isAllSeen == true){
                            viewPinkGradient.setBackgroundResource(R.drawable.border_gray_corner10)
                        } else{
                            viewPinkGradient.setBackgroundResource(R.drawable.gradient_background_corner10)
                        }

                        // Time
                        val timeLast = userLiveMomentsModel?.timeLatest ?: 0
                        tvTime.text = if (timeLast.toLong() > 0) {
                            timeLast.toLocalDateTime().timeAgoSince()
                        }else {
                            ""
                        }

                    }
                    TYPE_EMPTY -> {
                        layoutAddLiveMoment.visibility = View.GONE
                        layoutLiveMoment.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    }
                }
            }

        }

    }

}