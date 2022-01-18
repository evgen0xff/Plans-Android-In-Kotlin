package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.post.PostWebService

class PostDetailsVM(application: Application) : ListBaseVM(application) {

    var eventId: String? = null
    var postId: String? = null
    var eventModel: EventModel? = null
    var postModel: PostModel? = null

    var listComments = ArrayList<PostModel>()
    var messagePosting = MutableLiveData<String?>()

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int) {

        val action = {
            if (isShownLoading) {
                BusyHelper.show(context)
            }
            PostWebService.getPostDetails(postId, eventId, pageNumber, count) { postDetails, message ->
                BusyHelper.hide()
                if (postDetails != null) {
                    updateData(postDetails, pageNumber, count)
                    didLoadData.value = true
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
        pageNumber = (listComments.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(post: PostModel?, page: Int = 1, count: Int = 20){
        postModel = post
        if (page == 1) {
            listComments.clear()
        }
        listComments.replace(postModel?.comments, page, count)
    }

    fun sendPost(context: Context) {
        val commentText = messagePosting.value ?: return
        BusyHelper.show(context)
        PostWebService.createComment(postId, eventId, commentText) { success, message ->
            BusyHelper.hide()
            if (success) {
                messagePosting.value = null
                getAllList()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

}