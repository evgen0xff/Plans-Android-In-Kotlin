package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.PostLikedModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.post.PostWebService

class PostsLikedVM(application: Application) : ListBaseVM(application) {

    var listPosts = ArrayList<PostLikedModel>()

    override fun getList(isShownLoading: Boolean,
                     pageNumber: Int,
                     count: Int
    ) {
        if(isShownLoading)
            BusyHelper.show(context)

        PostWebService.getPostsLiked(pageNumber, count){ list, message ->
            BusyHelper.hide()
            if (list != null) {
                updateData(list, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }


    override fun getNextPage(context: Context,
                         isShownLoading: Boolean){
        super.getNextPage(context, isShownLoading)
        pageNumber = (listPosts.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(list: ArrayList<PostLikedModel>?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listPosts.clear()
        }
        listPosts.replace(list, page, count)
    }

}