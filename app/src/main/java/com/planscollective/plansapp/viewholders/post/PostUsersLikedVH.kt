package com.planscollective.plansapp.viewholders.post

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.UsersLikedAdapter
import com.planscollective.plansapp.databinding.CellPostUsersLikedBinding
import com.planscollective.plansapp.extension.setLayoutWidth
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class PostUsersLikedVH(
    private val itemBinding: CellPostUsersLikedBinding,
    var listener: PlansActionListener? = null
) : BaseViewHolder<PostModel>(itemBinding.root){

    private var countItems = 8
    private val widthItemCell : Int
        get() {
            return ((OSHelper.widthScreen - 22.toPx()) / countItems.toFloat()).toInt()
        }

    private var adapterUsers = UsersLikedAdapter()
    private var postModel: PostModel? = null
    private var eventModel: EventModel? = null


    init {
        while (widthItemCell < 50.toPx()) {
            countItems -= 1
        }
        val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        itemBinding.apply {
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapterUsers
            adapterUsers.listener = listener

            layoutHeart.setOnSingleClickListener{
                listener?.onClickLikeContent(postModel, event = eventModel, isLike = !(postModel?.isLike ?: false))
            }
            layoutHeart.setLayoutWidth(widthItemCell)

            layoutMoreUsers.setOnSingleClickListener {
                listener?.onClickMoreUsersLiked(postModel, eventModel)
            }
            layoutMoreUsers.setLayoutWidth(widthItemCell)
        }

    }

    override fun bind(item: PostModel?, data: Any?, isLast: Boolean) {
        postModel = item
        eventModel = (data as? HashMap<*, *>)?.get("event") as? EventModel
        itemBinding.apply {

            val countLiked = postModel?.likes?.size ?: 0

            // Header
            tvHeader.text = if (countLiked == 0) "Like" else {
                if (countLiked > 1) "$countLiked Likes" else "1 Like"
            }

            // Like/Unlike
            if (postModel?.isLike == true) {
                imvHeart.setImageResource(R.drawable.ic_heart_filled_green)
            }else {
                imvHeart.setImageResource(R.drawable.ic_heart_outline_grey)
            }

            // More Users
            val isAddMore = countLiked > (countItems - 1)
            var countShownUsersLiked = 0
            layoutMoreUsers.visibility = if (isAddMore){
                countShownUsersLiked = countItems - 2
                View.VISIBLE
            } else {
                countShownUsersLiked = countItems - 1
                View.GONE
            }

            // Users Liked
            val listUsers = ArrayList<UserModel>()
            postModel?.likes?.forEach {
                if (listUsers.size < countShownUsersLiked) {
                    listUsers.add(it)
                }
            }
            adapterUsers.updateAdapter(listUsers, widthItemCell, listener)
        }

    }

}