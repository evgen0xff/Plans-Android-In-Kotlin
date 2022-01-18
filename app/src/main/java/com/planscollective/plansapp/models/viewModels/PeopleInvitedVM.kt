package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.CountResponseModel
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.event.PeopleResponseModel

class PeopleInvitedVM(application: Application) : ListBaseVM(application) {

    enum class JoinType(val value: Int) {
        INVITED(1) {
            override val title = "INVITED"
        },
        GOING(2){
            override val title = "GOING"
        },
        MAYBE(3){
            override val title = "MAYBE"
        },
        NEXT_TIME(4){
            override val title = "NEXT TIME"
        },
        LIVE(5){
            override val title = "LIVE"
        },
        ATTENDED(6){
            override val title = "ATTENDED"
        };

        abstract val title : String;
    }

    var eventId: String? = null
    var eventModel: EventModel? = null
    var coutModel: CountResponseModel? = null
    var listUsers = ArrayList<UserModel>()

    var userTypes = arrayListOf(JoinType.LIVE, JoinType.INVITED, JoinType.GOING, JoinType.MAYBE, JoinType.NEXT_TIME)
    var selectedType = MutableLiveData<JoinType>()

    val selectedTabIndex : Int
        get() = selectedType.value?.let { userTypes.indexOf(it) } ?: 0

    val listTabItems : ArrayList<String>
        get() = userTypes.map { it ->
            val count : Int? = when(it) {
                JoinType.INVITED -> {
                    coutModel?.countInvited
                }
                JoinType.GOING -> {
                    coutModel?.countGoing
                }
                JoinType.LIVE -> {
                    coutModel?.countLive
                }
                JoinType.MAYBE -> {
                    coutModel?.countMaybe
                }
                JoinType.NEXT_TIME -> {
                    coutModel?.countNextTime
                }
                else -> { null }
            }
            it.title + (count?.takeIf { it > 0 }?.let {" ($it)"} ?: "")
        }.toArrayList()


    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int

    ) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }

        EventWebService.getPeople(eventId, selectedType.value?.value ?: 1, pageNumber, count, keywordSearch) {
            people, message ->
            BusyHelper.hide()
            if (people != null) {
                updateData(people, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listUsers.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(people: PeopleResponseModel?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listUsers.clear()
        }
        eventModel = people?.event
        coutModel = people?.count
        listUsers.replace(people?.people, page, count)

        if (selectedType.value == null) {
            selectedType.value = getInvitedType()
        }else {
            didLoadData.value = true
        }
    }

    fun getInvitedType() : JoinType {
        return if (eventModel?.isExistLiveGuest() == true) {
            JoinType.LIVE
        }else if (eventModel?.isExistAcceptGuest() == true) {
            JoinType.GOING
        }else {
            JoinType.INVITED
        }
    }

}