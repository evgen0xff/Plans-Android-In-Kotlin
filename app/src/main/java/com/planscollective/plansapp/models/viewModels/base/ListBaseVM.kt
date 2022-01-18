package com.planscollective.plansapp.models.viewModels.base

import android.app.Application
import android.content.Context
import android.view.View

abstract class ListBaseVM(application: Application) : PlansBaseVM(application) {
    open var pageNumber: Int = 1
    open var numberOfRows: Int = 20
    open var keywordSearch: String = ""
    open var actionEnterKey: ((view: View) -> Unit)? = null

    open fun getList(isShownLoading: Boolean = true,
                     pageNumber: Int = 1,
                     count: Int = 20
    ) {
    }

    open fun getAllList(isShownLoading: Boolean = true) {
        getList(isShownLoading, 1, pageNumber * numberOfRows)
    }

    open fun getNextPage(context: Context,
                         isShownLoading: Boolean = false){

    }

}