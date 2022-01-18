package com.planscollective.plansapp.fragment.base

import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.planscollective.plansapp.models.viewModels.AuthViewModel

abstract  class AuthBaseFragment<VBinding: ViewBinding> : BaseFragment<VBinding>() {

    val authVM: AuthViewModel by activityViewModels()

}