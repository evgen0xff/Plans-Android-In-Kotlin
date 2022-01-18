package com.planscollective.plansapp.models.viewModels.base

import android.app.Application
import android.content.Context
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.PlansApp

open class PlansBaseVM(application: Application) : AndroidViewModel(application), Observable {

    val context: Context?
        get() = getApplication<PlansApp>().currentActivity

    open val didLoadData = MutableLiveData<Boolean>()

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    fun notifyChange(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

}
