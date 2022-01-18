package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.post.PostWebService

class EventDetailsVM(application: Application) : ListBaseVM(application) {

    var eventId: String? = null
    var eventModel: EventModel? = null
    var listPosts = ArrayList<PostModel>()
    var messagePosting = MutableLiveData<String?>()
    var urlPhotoPosting: String? = null
    var urlVideoPosting: String? = null
    var bmpVideoThumb: Bitmap? = null
    val childSizesMap = mutableMapOf<Int, Int>()

    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        EventWebService.getEventDetails(eventId, pageNumber, count){ event, message ->
            BusyHelper.hide()
            if (event != null) {
                updateData(event, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listPosts.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(event: EventModel?, page: Int = 1, count: Int = 20){
        eventModel = event
        if (page == 1) {
            listPosts.clear()
        }
        listPosts.replace(eventModel?.posts, page, count)
    }

    fun createPost() {

        var msgSuccess = ""
        val mediaType = if (!urlVideoPosting.isNullOrEmpty()){
            msgSuccess = "Your video was posted."
            "video"
        } else if (!urlPhotoPosting.isNullOrEmpty()){
            msgSuccess = "Your photo was posted."
            "image"
        } else{
            msgSuccess = "Your comment was posted."
            "text"
        }

        val urlMedia = urlVideoPosting ?: urlPhotoPosting

        ToastHelper.showLoadingAlerts(ToastHelper.LoadingToastType.POSTING)
        PostWebService.createPost(eventId, messagePosting.value, mediaType, urlMedia) { success, message ->
            ToastHelper.hideLoadingAlerts(ToastHelper.LoadingToastType.POSTING)

            val msg = if (success) msgSuccess else message
            ToastHelper.showMessage(msg)

            PLANS_APP.currentFragment?.refreshAll(false)

            AnalyticsManager.logEvent(AnalyticsManager.EventType.POST_ADD)
        }

        urlVideoPosting = null
        urlPhotoPosting = null
        messagePosting.value = null
        didLoadData.value = true
    }


}