package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.LiveMomentModel
import com.planscollective.plansapp.models.dataModels.UserLiveMomentsModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.liveMoment.LiveMomentWebService

class WatchLiveMomentsVM(application: Application) : ListBaseVM(application) {

    enum class Status {
        NONE,
        PHOTO_PLAYING,
        PHOTO_PAUSE,
        VIDEO_PLAYING,
        VIDEO_PAUSE
    }


    var eventId: String? = null
    var userId: String? = null
    var liveMomentId: String? = null

    var eventModel: EventModel? = null
    var userModel: UserModel? = null
    var liveMomentModel: LiveMomentModel? = null

    var listUserLiveMoments = ArrayList<UserLiveMomentsModel>()
    var listLiveMoments = ArrayList<LiveMomentModel>()
    var curLiveMoment: LiveMomentModel? = null
    var curUserMomentModel: UserLiveMomentsModel? = null

    var curStatus = MutableLiveData<Status>()
    var curIndex = 0
    var isMine = false
    var isEventHost = false


    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int

    ) {
        val eventId = eventId ?: eventModel?.id ?: return

        val action = {
            if (isShownLoading) {
                BusyHelper.show(context)
            }
            LiveMomentWebService.getLiveMoments(eventId, userId, liveMomentId){ list, message ->
                BusyHelper.hide()
                if (list != null) {
                    updateData(list)
                }else {
                    ToastHelper.showMessage(message)
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
        pageNumber = (listUserLiveMoments.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    fun updateData(list: ArrayList<UserLiveMomentsModel>?, page: Int = 1, count: Int = 20){
        listUserLiveMoments.clear()
        list?.let {
            listUserLiveMoments.addAll(it)
        }

        didLoadData.value = if (listUserLiveMoments.size > 0) {
            curIndex = 0
            initializeData()
            curStatus.value = Status.NONE
            true
        }else {
            false
        }
    }

    fun initializeData() {
        userId = userId ?: userModel?.id
        liveMomentId = liveMomentId ?: liveMomentModel?.id

        listLiveMoments.clear()
        listUserLiveMoments.forEach {
            it.liveMoments?.let{
                listLiveMoments.addAll(it)
            }
        }
    }

    fun prepareData(index: Int = 0) {
        val curIndex = index.takeIf { it >=0 && it < listLiveMoments.size } ?: return
        curLiveMoment = listLiveMoments[curIndex]
        curUserMomentModel = listUserLiveMoments.firstOrNull { it.liveMoments?.any { it.id == curLiveMoment?.id } ?: false }
        isMine = curUserMomentModel?.user?.id == UserInfo.userId
        isEventHost = eventModel?.userId == UserInfo.userId
    }

    fun viewedLiveMoment(liveMoment: LiveMomentModel? = null) {
        val id = liveMoment?.id ?: curLiveMoment?.id ?: return
        val listIds = arrayListOf(id)
        LiveMomentWebService.viewedLiveMoment(listIds)
    }

}