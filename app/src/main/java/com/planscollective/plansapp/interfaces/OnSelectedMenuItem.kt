package com.planscollective.plansapp.interfaces

import com.planscollective.plansapp.models.dataModels.MenuModel

interface OnSelectedMenuItem {
    fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any? = null)
}