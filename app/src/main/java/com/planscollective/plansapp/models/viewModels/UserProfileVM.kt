package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import android.util.Size
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.user.UserWebService

enum class EventType(val value: Int) {
    ORGANIZED(0) {
        override val keyValue = "hosting"
    },
    ATTENDING(1){
        override val keyValue = "attending"
    },
    SAVED(2){
        override val keyValue = "saved"
    };

    abstract val keyValue : String;
}


class UserProfileVM(application: Application) : ListBaseVM(application) {

    var userId : String? = null
    var user : UserModel? = null
    var selectedType = MutableLiveData<EventType>()
    var listEvents = ArrayList<EventModel>()
    var heightProfile = 320
    val childSizesMap = mutableMapOf<Int, Int>()


    override fun getList(
        isShownLoading: Boolean,
        pageNumber: Int,
        count: Int
    ){
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        EventWebService.getEventList(pageNumber, count, type = selectedType.value?.keyValue, userId = userId, mobile = user?.mobile){ list, message ->
            BusyHelper.hide()
            if (list != null) {
                updateData(list, pageNumber, count)
                didLoadData.value = true
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    override fun getAllList(isShownLoading: Boolean) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        UserWebService.getUserProfile(userId) { userModel, message ->
            BusyHelper.hide()
            if (userModel != null) {
                if (updateUserInfo(userModel)) {
                    super.getAllList(isShownLoading)
                }else {
                    PLANS_APP.gotoBack()
                    ToastHelper.showMessage("This user is unavailable on Plans")
                }
            }else {
                ToastHelper.showMessage(message)
            }
        }

    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listEvents.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(list: ArrayList<EventModel>?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listEvents.clear()
        }
        listEvents.replace(list, page, count)
    }

    private fun updateUserInfo(userModel: UserModel?) : Boolean {
        user = userModel
        userId = userModel?.id

        if (userId == UserInfo.userId) {
            updateMyAccount(userModel)
        }

        return user?.isActive ?: false
    }

    private fun updateMyAccount(userModel: UserModel?) {
        UserInfo.isPrivateAccount = userModel?.isPrivateAccount ?: true

        userModel?.coinNumber?.takeIf { it != UserInfo.coinNumber }?.also {
            UserInfo.coinNumber = it
            it.takeIf { it > 0 && it <= Constants.arrayStarsLarge.size }?.also{ it1 ->
                ToastHelper.showOverlay(
                    ConstantTexts.CONG_NEW_COIN,
                    Constants.arrayStarsLarge[it1 - 1],
                    Size(100.toPx(), 100.toPx())
                )
            }
        }
    }

}