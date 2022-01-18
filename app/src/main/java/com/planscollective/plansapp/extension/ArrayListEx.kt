package com.planscollective.plansapp.extension

import java.util.*

fun <T : Any> ArrayList<T>.replace(newArray: ArrayList<T>?, page: Int, count: Int) {
    newArray?.let {
        if (page > 0 && count > 0) {
            var index = count * (page - 1)
            for (i in 0 until count){
                if (index < this.size){
                    this.removeAt(index)
                }
            }
            if (index > this.size) {
                index = this.size
            }
            this.addAll(index, it)
        }
    }
}

fun <T> List<T>.toArrayList() : ArrayList<T> = ArrayList(this)
