package com.planscollective.plansapp.fragment.event

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.BoundaryAdapter
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentEditEventBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.EditEventVM
import com.planscollective.plansapp.models.viewModels.EditInvitationVM
import com.planscollective.plansapp.models.viewModels.LocationSelectionVM
import com.planscollective.plansapp.webServices.event.EventWebService
import permissions.dispatcher.*
import java.util.*

@RuntimePermissions
class EditEventFragment : PlansBaseFragment<FragmentEditEventBinding>(), MediaPickerListener,
    PlansEditTextViewListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
    OnMapReadyCallback, OnItemTouchListener {

    private val viewModel : EditEventVM by viewModels()
    private val args: EditEventFragmentArgs by navArgs()
    private var calendar = Calendar.getInstance()
    private var selectedEditText: PlansEditTextView? = null
    private var mapFragment : SupportMapFragment? = null
    private var mapGoogle: GoogleMap? = null
    private var adapterBoundary = BoundaryAdapter()
    private var listInvitedUser = ArrayList<ImageView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        viewModel.eventId = args.eventId
        viewModel.isDuplicate = args.isDuplicate
        viewModel.eventModel = args.eventModel

        if (viewModel.eventModel?.checkInRange == null || viewModel.eventModel?.checkInRange == 0) {
            viewModel.eventModel?.checkInRange = 300
        }

        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            if (it) {
                updateUI()
            }
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

        curBackStackEntry?.savedStateHandle?.getLiveData<PlaceModel>(Keys.SELECTED_LOCATION)?.observe(this){
            changeLocation(it)
        }

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Event Cover Image/Video UI
        binding.layoutEventCover.setOnSingleClickListener(this)

        // Start/End Date/Time, Location and Caption UIs
        binding.also {
            it.etEventName.listener = this
            it.etDetails.listener = this
            it.etStartDate.listener = this
            it.etEndDate.listener = this
            it.etStartTime.listener = this
            it.etEndTime.listener = this
            it.etLocation.listener = this
            it.etCaption.listener = this
        }

        // Location Boundary
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBoundary.layoutManager = layoutManager
        binding.rvBoundary.adapter = adapterBoundary

        // Map View
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Invited Friends UI
        binding.apply {
            listInvitedUser.clear()
            listInvitedUser.add(imvUserImage1)
            listInvitedUser.add(imvUserImage2)
            listInvitedUser.add(imvUserImage3)
            layoutInvitedFriends.setOnSingleClickListener(this@EditEventFragment)
        }

        // Options
        binding.also {
            it.layoutPublic.setOnSingleClickListener(this)
            it.layoutPrivate.setOnSingleClickListener(this)
            it.layoutGroupChatOn.setOnSingleClickListener(this)
            it.layoutGroupChatOff.setOnSingleClickListener(this)
            it.btnDone.setOnSingleClickListener(this)
        }

        // Loading Data from server
        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    private fun updateUI() {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)

        binding.viewModel = viewModel

        // Event Cover - Image/Video
        updateEventCoverUI()

        // Edit Texts UI - Event Name, Event Details, Event Caption
        updateEditTextsUI()

        // Location UI
        updateLocationUI()

        // Update Invited Friends UI
        updateInvitedFriendsUI()

        // Update Options UI
        updateOptionsUI()

        // Update Done UI
        updateDoneUI()
    }

    private fun updateOptionsUI() {
        binding.apply {
            (viewModel?.eventModel?.isPublic ?: false).also {
                btnPublic.isSelected = it
                btnPrivate.isSelected = !it
            }

            (viewModel?.eventModel?.isGroupChatOn ?: false).also {
                btnGroupChatOn.isSelected = it
                btnGroupChatOff.isSelected = !it
            }
        }
    }

    private fun updateInvitedFriendsUI(eventNew: EventModel? = null) {
        binding.layoutInvitation.visibility = if (viewModel?.isDuplicate) View.VISIBLE else View.GONE

        listInvitedUser.forEach { it.visibility = View.GONE }

        // Invited People
        val removedCounts = viewModel?.eventModel?.getRemovedUserCounts(eventNew?.invitations, eventNew?.invitedPeople) ?: Triple(0, 0, 0)
        eventNew?.invitations?.also { viewModel?.eventModel?.invitations = it}
        eventNew?.invitedPeople?.also { viewModel?.eventModel?.invitedPeople = it}
        val selectedCounts = viewModel?.eventModel?.getInvitedUserCounts() ?: Triple(0, 0, 0)

        binding.apply {
            getSpanTextForEditing(selectedCounts.first, selectedCounts.second, selectedCounts.third, true)?.let {
                tvSelectedCounts.text = it
                tvSelectedCounts.visibility = View.VISIBLE
            } ?: run{
                tvSelectedCounts.visibility = View.GONE
            }

            getSpanTextForEditing(removedCounts.first, removedCounts.second, removedCounts.third, false)?.let {
                tvRemovedCounts.text = it
                tvRemovedCounts.visibility = View.VISIBLE
            } ?: run{
                tvRemovedCounts.visibility = View.GONE
            }

            viewModel?.eventModel?.invitations?.takeIf { it.isNotEmpty() }?.also {
                for (i in 0 until it.size) {
                    if (i < 3) {
                        listInvitedUser[i].visibility = View.VISIBLE
                        listInvitedUser[i].setUserImage(it[i].profileImage)
                    }else {
                        break
                    }
                }
            }
        }
    }

    private fun updateEventCoverUI() {
        val event = viewModel.eventModel ?: return
        binding.apply {
            // Event Cover Image/Video
            videoView.visibility = if (event.mediaType == "video"){
                videoView.playVideoUrl(event.imageOrVideo, event.thumbnail)
                View.VISIBLE
            }else {
                imgvEventCover.setEventImage(event.imageOrVideo)
                View.GONE
            }
        }
    }

    private fun updateEditTextsUI() {
        binding.apply {
            etEventName.text = viewModel?.eventModel?.eventName
            etDetails.text = viewModel?.eventModel?.details
            etStartDate.text = viewModel?.eventModel?.startTime?.toLocalDateTime()?.toFormatString("MMMM dd, yyyy")
            etEndDate.text = viewModel?.eventModel?.endTime?.toLocalDateTime()?.toFormatString("MMMM dd, yyyy")
            etStartTime.text = viewModel?.eventModel?.startTime?.toLocalDateTime()?.toFormatString("hh:mm a")
            etEndTime.text = viewModel?.eventModel?.endTime?.toLocalDateTime()?.toFormatString("hh:mm a")
            etCaption.text = viewModel?.eventModel?.caption
        }
    }

    private fun updateDoneUI(){
        binding.apply {
            btnDone.isClickable = if (viewModel?.checkValidData() == true) {
                btnDone.background = ContextCompat.getDrawable(PLANS_APP, R.drawable.button_bkgnd_purple)
                true
            }else {
                btnDone.background = ContextCompat.getDrawable(PLANS_APP, R.drawable.button_bkgnd_gray)
                false
            }
        }
    }

    private fun showDatePicker(editText: PlansEditTextView?) {
        selectedEditText = editText
        var minDate = Date()
        val startTime = viewModel.eventModel?.startTime?.toDate() ?: Date(Date().time + 60 * 1000)
        when (editText) {
            binding.etStartDate -> {
                minDate = Date(Date().time + 60 * 1000)
                calendar.time = if (startTime.time >= minDate.time) startTime else minDate
            }
            binding.etEndDate -> {
                val endTime = viewModel.eventModel?.endTime?.toDate() ?: Date(startTime.time + 30 * 60 * 1000)
                val now = Date(Date().time + 60 * 1000)
                minDate = Date(startTime.time + 30 * 60 * 1000)
                minDate = if (minDate.time < now.time) now else minDate
                calendar.time = if (endTime.time > minDate.time) endTime else minDate
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDlg = DatePickerDialog(requireActivity(), this, year, month, day)

        datePickerDlg.datePicker.minDate = minDate.time

        datePickerDlg.show()
        datePickerDlg.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(PlansColor.PINK_MAIN)
        datePickerDlg.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(PlansColor.PINK_MAIN)
    }

    private fun showTimePicker(editText: PlansEditTextView?) {
        selectedEditText = editText

        val now = Date(Date().time + 60 * 1000)

        binding.apply {
            when (editText) {
                etStartTime -> {
                    calendar.time = viewModel?.eventModel?.startTime?.toDate() ?: now
                }
                etEndTime -> {
                    calendar.time = viewModel?.eventModel?.endTime?.toDate() ?: now
                }
            }
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), this, hour, minute, false).show()
    }

    private fun changeLocation(place: PlaceModel?) {
        viewModel.eventModel?.address = place?.formattedAddress ?: place?.address
        viewModel.eventModel?.locationName = place?.name
        viewModel.eventModel?.lat = place?.latitude
        viewModel.eventModel?.long = place?.longitude
        updateLocationUI()
    }

    private fun updateLocationUI() {
        // Location Boundaries
        adapterBoundary.updateAdapter(viewModel.boundaries, this, viewModel.selectedBoundaryItem)

        binding.apply {
            etLocation.text = if (viewModel?.eventModel?.locationName.isNullOrEmpty()) viewModel?.eventModel?.address else viewModel?.eventModel?.locationName

            val action = {
                layoutMap.visibility = View.INVISIBLE
            }
            val lat = viewModel?.eventModel?.lat?.toDouble() ?: run{
                action()
                return
            }
            val long = viewModel?.eventModel?.long?.toDouble() ?: run{
                action()
                return
            }
            val range = viewModel?.eventModel?.checkInRange?.toDouble() ?: run{
                action()
                return
            }
            if (lat == 0.0 && long == 0.0) run{
                action()
                return
            }

            mapGoogle?.clear()

            val locationEvent = LatLng(lat, long)

            mapGoogle?.addCircle(
                CircleOptions()
                    .center(locationEvent)
                    .radius(range)
                    .fillColor(PlansColor.TEAL_MAP_CIRCLE)
                    .strokeWidth(0f)
            )

            mapGoogle?.addMarker(
                MarkerOptions()
                    .position(locationEvent)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_map_purple_filled))
            )

            mapGoogle?.moveCamera(CameraUpdateFactory.newLatLngZoom(locationEvent, 15f))
        }
    }

    private fun createEvent() {
        val event = viewModel.getEventWith() ?: return
        BusyHelper.show(requireContext(), timeDelay = 0)
        EventWebService.createEvent(event){
                success, message ->
            BusyHelper.hide()
            if (success) {
                ToastHelper.showMessage("The event was duplicated.")
                AnalyticsManager.logEvent(AnalyticsManager.EventType.CREATE_EVENT)
                gotoBack()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun updateEvent() {
        val event = viewModel.getEventWith() ?: return
        BusyHelper.show(requireContext(), timeDelay = 0)
        EventWebService.updateEvent(event){
                success, message ->
            BusyHelper.hide()
            if (success) {
                ToastHelper.showMessage("You updated the event.")
                gotoBack()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun actionDone() {
        if (viewModel.checkValidData(true)) {
            if (viewModel.isDuplicate) {
                createEvent()
            }else {
                updateEvent()
            }
        }
    }


    //******************************* Permissions for Camera and Gallery **************************************//
    @NeedsPermission(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showCameraGallery() {
        MediaPickerHelper.showPickerOptions(this, this, MediaPickerHelper.PickerType.EVENT_COVER)
    }

    @OnShowRationale(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForPermissions(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionNeverAskAgain() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    //********************************* PlansEditTextViewListener **************************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        binding.apply {
            when(editText) {
                etEventName -> {
                    viewModel?.eventModel?.eventName = text
                    updateDoneUI()
                }
                etDetails -> {
                    viewModel?.eventModel?.details = text
                }
                etCaption -> {
                    viewModel?.eventModel?.caption = text
                }
                else -> {}
            }
        }
    }

    override fun didClicked(editText: PlansEditTextView?) {
        binding.apply {
            when(editText) {
                etStartDate -> {
                    hideKeyboard()
                    showDatePicker(etStartDate)
                }
                etEndDate -> {
                    hideKeyboard()
                    showDatePicker(etEndDate)
                }
                etStartTime -> {
                    hideKeyboard()
                    showTimePicker(etStartTime)
                }
                etEndTime -> {
                    hideKeyboard()
                    showTimePicker(etEndTime)
                }
                etLocation -> {
                    hideKeyboard()
                    gotoLocationSelection(LocationSelectionVM.LocationSelectionType.EDIT_EVENT)
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
                layoutEventCover -> {
                    showCameraGallery()
                }
                layoutInvitedFriends -> {
                    gotoEditInvitation(viewModel?.eventModel, if (viewModel?.isDuplicate == true) EditInvitationVM.EditMode.CREATE else EditInvitationVM.EditMode.EDIT)
                }
                layoutPublic -> {
                    viewModel?.eventModel?.isPublic = true
                    updateOptionsUI()
                }
                layoutPrivate -> {
                    viewModel?.eventModel?.isPublic = false
                    updateOptionsUI()
                }
                layoutGroupChatOn -> {
                    viewModel?.eventModel?.isGroupChatOn = true
                    updateOptionsUI()
                }
                layoutGroupChatOff -> {
                    viewModel?.eventModel?.isGroupChatOn = false
                    updateOptionsUI()
                }
                btnDone -> {
                    actionDone()
                }
            }
        }
    }

    //********************************* MediaPickerListener **************************************//
    override fun onSelectedPhoto(filePath: String?) {
        viewModel.eventModel?.imageOrVideo = filePath
        viewModel.eventModel?.mediaType = "image"
        viewModel.isNewMedia = true
        updateEventCoverUI()
    }

    override fun onSelectedVideo(filePath: String?) {
        viewModel.eventModel?.imageOrVideo = filePath
        viewModel.eventModel?.mediaType = "video"
        viewModel.isNewMedia = true
        updateEventCoverUI()
    }


    //********************************* OnDateSetListener **************************************//

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val now = Date(Date().time + 60 * 1000)
        binding.apply {
            when(selectedEditText) {
                etStartDate -> {
                    val startTime = (viewModel?.eventModel?.startTime?.toDate() ?: now).let {
                        calendar.setCalendarWithTime(it).timeInMillis / 1000
                    }
                    viewModel?.eventModel?.startDate = startTime
                    viewModel?.eventModel?.startTime = startTime

                    val delta = (viewModel?.eventModel?.endTime?.toLong() ?: (now.time / 1000)) - startTime
                    if (delta < 30 * 60) {
                        viewModel?.eventModel?.endTime = startTime + 30 * 60
                        viewModel?.eventModel?.endDate = viewModel?.eventModel?.endTime
                    }
                    updateEditTextsUI()
                }
                etEndDate -> {
                    var endTime = (viewModel?.eventModel?.endTime?.toDate() ?: now).let {
                        calendar.setCalendarWithTime(it).timeInMillis / 1000
                    }
                    val startTime = (viewModel?.eventModel?.startTime?.toDate() ?: now).time / 1000
                    if ((endTime - startTime) < 30 * 60) {
                        endTime = startTime + 30 * 60
                    }
                    viewModel?.eventModel?.endDate = endTime
                    viewModel?.eventModel?.endTime = endTime
                    updateEditTextsUI()
                }
            }
        }

    }

    //************************* TimePickerDialog.OnTimeSetListener *******************************//

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute

        val now = Date()
        val min = Date(now.time + 60 * 1000)

        binding.apply {
            when (selectedEditText) {
                etStartTime -> {
                    if (calendar.timeInMillis >= min.time){
                        viewModel?.eventModel?.startTime = calendar.timeInMillis / 1000
                        viewModel?.eventModel?.startDate = calendar.timeInMillis / 1000
                        val delta = (viewModel?.eventModel?.endTime ?: min.time / 1000).toLong() - (viewModel?.eventModel?.startTime ?: min.time / 1000).toLong()
                        if ( delta < (30 * 60) ) {
                            viewModel?.eventModel?.endTime = (viewModel?.eventModel?.startTime?.toLong() ?: 0) + 30 * 60
                            viewModel?.eventModel?.endDate = viewModel?.eventModel?.endTime
                        }
                        updateEditTextsUI()
                    } else {
                        ToastHelper.showMessage(ConstantTexts.START_TIME_CANNOT_BEFORE_NOW)
                    }
                }
                etEndTime -> {
                    val startTime = viewModel?.eventModel?.startTime?.toLong() ?: min.time / 1000
                    val delta = (calendar.timeInMillis / 1000) - startTime
                    if (delta <= 0) {
                        ToastHelper.showMessage(ConstantTexts.START_TIME_CANNOT_AFTER_END_TIME)
                    }else if (delta < 30 * 60) {
                        ToastHelper.showMessage(ConstantTexts.END_TIME_GREATER_START_30)
                    }else {
                        viewModel?.eventModel?.endTime = calendar.timeInMillis / 1000
                        viewModel?.eventModel?.endDate = viewModel?.eventModel?.endTime
                        updateEditTextsUI()
                    }
                }
            }
        }


    }

    //********************************* OnMapReadyCallback **************************************//

    override fun onMapReady(googleMap: GoogleMap) {
        mapGoogle = googleMap
        updateLocationUI()
    }

    //********************************* Boundary OnItemClick **************************************//

    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        if (viewModel.selectedBoundaryItem != position) {
            viewModel.selectedBoundaryItem = position
            updateLocationUI()
        }
    }

}