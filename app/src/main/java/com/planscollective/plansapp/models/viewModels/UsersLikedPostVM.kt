package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.models.dataModels.UserLikedModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.post.PostWebService

class UsersLikedPostVM(application: Application) : ListBaseVM(application) {

    var postId: String? = null
    var eventId: String? = null
    var postModel: PostModel? = null
    var eventModel: EventModel? = null
    var listUsersLiked = ArrayList<UserModel>()
    val listSearched = ArrayList<UserModel>()

    override fun getList(
        isShownLoading: Boolean,
        pageNumber: Int,
        count: Int) {

        val action = {
            if (isShownLoading) {
                BusyHelper.show(context)
            }
            PostWebService.getPostDetails(postId, eventId) { postDetails, message ->
                BusyHelper.hide()
                if (postDetails != null) {
                    updateData(postDetails, pageNumber, count)
                }else {
                    PLANS_APP.gotoBack()
                }
            }
        }

        if (eventModel == null) {
            if (isShownLoading) {
                BusyHelper.show(context)
            }
            EventWebService.getEventDetails(eventId){ event, message ->
                BusyHelper.hide()
                if (event != null) {
                    eventModel = event
                    action()
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        }else {
            action()
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listUsersLiked.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(post: PostModel?, page: Int = 1, count: Int = 20){
        postModel = post
        listUsersLiked.clear()
        postModel?.likes?.sortedWith(compareBy<UserLikedModel> { user ->
            when(user.friendShipStatus) {
                0 -> if (user.friendRequestSender != UserInfo.userId) 1 else 2
                1 -> 0  // Friends
                else -> if (user.id != UserInfo.userId) 3 else 4
            }
        }.thenBy { "${it.firstName ?: ""} ${it.lastName ?: ""}" })?.also{
            listUsersLiked.addAll(it)
        }

        searchUsers()
    }

    fun searchUsers() {
        listSearched.clear()
        keywordSearch.trim().takeIf{it.isNotEmpty()}?.also {
            listSearched.addAll(listUsersLiked.filter{ it1 -> (it1.fullName ?: it1.name ?: "${it1.firstName ?: ""} ${it1.lastName ?: ""}").lowercase().contains(it.lowercase())})
        } ?: run {
            listSearched.addAll(listUsersLiked)
        }
        didLoadData.value = true
    }


}