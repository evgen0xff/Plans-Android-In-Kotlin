package com.planscollective.plansapp.interfaces

interface PlansItemMoveListener {
    fun onItemSelected(position: Int?, data: Any? = null){}
    fun onItemMove(position: Int?, data: Any? = null){}

}