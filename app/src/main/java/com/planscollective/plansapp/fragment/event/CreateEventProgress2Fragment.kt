package com.planscollective.plansapp.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentCreateEventProgress2Binding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.viewModels.CreateEventVM
import com.planscollective.plansapp.models.viewModels.EditInvitationVM
import permissions.dispatcher.*
import java.util.*

class CreateEventProgress2Fragment : PlansBaseFragment<FragmentCreateEventProgress2Binding>(),
    PlansEditTextViewListener {

    private val viewModel : CreateEventVM by navGraphViewModels(R.id.createEventFragment)
    private var listInvitedUser = ArrayList<View>()
    private var listInvitedUserImage = ArrayList<ImageView>()
    override var screenName: String? = "CreateEvent_Screen_3"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateEventProgress2Binding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        curBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<InvitationModel>>(Keys.SELECTED_USERS)?.observe(this){
            selectedUsers ->
            val eventNew = EventModel()
            eventNew.invitations = selectedUsers.filter { it.typeInvitation == InvitationModel.InviteType.FRIEND }.toArrayList()
            eventNew.invitedPeople = selectedUsers.filter { it.typeInvitation == InvitationModel.InviteType.MOBILE || it.typeInvitation == InvitationModel.InviteType.EMAIL }.toArrayList()
            updateInvitedFriendsUI(eventNew)
        }

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Caption UIs
        binding.also {
            it.etCaption.listener = this
        }

        // Invited Friends UI
        binding.apply {
            listInvitedUser.clear()
            listInvitedUser.add(layoutUser1)
            listInvitedUser.add(layoutUser2)
            listInvitedUser.add(layoutUser3)

            listInvitedUserImage.clear()
            listInvitedUserImage.add(imgvUser1)
            listInvitedUserImage.add(imgvUser2)
            listInvitedUserImage.add(imgvUser3)
            layoutInvitation.setOnSingleClickListener(this@CreateEventProgress2Fragment)
        }

        // Continue Button UI
        binding.btnContinue.setOnSingleClickListener(this)

        updateUI()
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        // Edit Texts UI - Event Name, Event Details, Event Caption
        updateEditTextsUI()

        // Update Invited Friends UI
        updateInvitedFriendsUI()
    }


    private fun updateInvitedFriendsUI(eventNew: EventModel? = null) {
        listInvitedUser.forEach { it.visibility = View.GONE }

        // Invited People
        eventNew?.invitations?.also { viewModel.eventModel.invitations = it}
        eventNew?.invitedPeople?.also { viewModel.eventModel.invitedPeople = it}
        val selectedCounts = viewModel.eventModel.getInvitedUserCounts()

        binding.apply {
            getSpanTextForEditing(selectedCounts.first, selectedCounts.second, selectedCounts.third, true)?.let {
                tvDescription.text = it
                tvDescription.visibility = View.VISIBLE
                layoutGuideEmpty.visibility = View.INVISIBLE
                layoutInvitedPeople.visibility = View.VISIBLE
            } ?: run{
                tvDescription.visibility = View.GONE
                layoutGuideEmpty.visibility = View.VISIBLE
                layoutInvitedPeople.visibility = View.INVISIBLE
            }

            viewModel?.eventModel?.invitations?.takeIf { it.isNotEmpty() }?.also {
                for (i in 0 until it.size) {
                    if (i < 3) {
                        listInvitedUser[i].visibility = View.VISIBLE
                        listInvitedUserImage[i].setUserImage(it[i].profileImage)
                    }else {
                        break
                    }
                }
            }
        }
    }

    private fun updateEditTextsUI() {
        binding.apply {
            etCaption.text = viewModel?.eventModel?.caption
        }
    }

    //********************************* PlansEditTextViewListener **************************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        binding.apply {
            when(editText) {
                etCaption -> {
                    viewModel?.eventModel?.caption = text
                }
                else -> {}
            }
        }
    }


    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                layoutInvitation -> {
                    gotoEditInvitation(viewModel?.eventModel, EditInvitationVM.EditMode.CREATE)
                }
                btnContinue -> {
                    val action = NavDashboardDirections.actionGlobalCreateEventProgress3Fragment()
                    navigate(directions = action)
                }
            }
        }
    }

}