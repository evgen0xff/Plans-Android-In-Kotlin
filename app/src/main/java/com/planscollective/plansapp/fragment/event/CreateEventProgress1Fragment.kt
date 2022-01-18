package com.planscollective.plansapp.fragment.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
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
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.BoundaryAdapter
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentCreateEventProgress1Binding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.CreateEventVM
import com.planscollective.plansapp.models.viewModels.LocationSelectionVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateEventProgress1Fragment : PlansBaseFragment<FragmentCreateEventProgress1Binding>(),
    PlansEditTextViewListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
    OnMapReadyCallback, OnItemTouchListener {

    private val viewModel : CreateEventVM by navGraphViewModels(R.id.createEventFragment)
    private var calendar = Calendar.getInstance()
    private var selectedEditText: PlansEditTextView? = null
    private var mapFragment : SupportMapFragment? = null
    private var mapGoogle: GoogleMap? = null
    private var adapterBoundary = BoundaryAdapter()
    override var screenName: String? = "CreateEvent_Screen_2"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateEventProgress1Binding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        if (viewModel.eventModel.checkInRange == null || viewModel.eventModel.checkInRange == 0) {
            viewModel.eventModel.checkInRange = 300
        }

        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        curBackStackEntry?.savedStateHandle?.getLiveData<PlaceModel>(Keys.SELECTED_LOCATION)?.observe(this){
            changeLocation(it)
        }

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Start/End Date/Time and Location UIs
        binding.also {
            it.etStartDate.listener = this
            it.etEndDate.listener = this
            it.etStartTime.listener = this
            it.etEndTime.listener = this
            it.etLocation.listener = this
        }

        // Location Boundary
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBoundary.layoutManager = layoutManager
        binding.rvBoundary.adapter = adapterBoundary

        // Map View
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Continue Button UI
        binding.btnContinue.setOnSingleClickListener(this)

        // Loading Data from server
        updateUI()
    }

    private fun updateUI() {
        lifecycleScope.launch (Dispatchers.Main){
            binding.viewModel = viewModel

            // Edit Texts UI - Event Name, Event Details, Event Caption
            updateEditTextsUI()

            // Location UI
            updateLocationUI()

            // Continue UI
            updateContinueUI()
        }
    }

    private fun updateContinueUI () {
        binding.apply {
            btnContinue.isClickable = if (validateData()) {
                btnContinue.background = ContextCompat.getDrawable(PLANS_APP, R.drawable.button_bkgnd_purple)
                true
            }else {
                btnContinue.background = ContextCompat.getDrawable(PLANS_APP, R.drawable.button_bkgnd_gray)
                false
            }
        }
    }

    private fun validateData(isShownAlert: Boolean = false) : Boolean {
        var result = true
        var errMsg : String? = null
        val now = Date().time / 1000

        if (viewModel.eventModel.startDate == null || viewModel.eventModel.startDate == 0) {
            result = false
            errMsg = ConstantTexts.YOUR_EVENT_NEED_START_DATE
        }else if (viewModel.eventModel.startTime == null || viewModel.eventModel.startTime == 0) {
            result = false
            errMsg = ConstantTexts.YOUR_EVENT_NEED_START_TIME
        }else if (viewModel.eventModel.endDate == null || viewModel.eventModel.endDate == 0) {
            result = false
            errMsg = ConstantTexts.YOUR_EVENT_NEED_END_DATE
        }else if (viewModel.eventModel.endTime == null || viewModel.eventModel.endTime == 0) {
            result = false
            errMsg = ConstantTexts.YOUR_EVENT_NEED_END_TIME
        }else if (((viewModel.eventModel.endTime ?: now).toLong() - (viewModel.eventModel.startTime ?: now).toLong()) < 30 * 60) {
            result = false
            errMsg = ConstantTexts.END_TIME_GREATER_START_30
        }

        if (!result && isShownAlert) {
            ToastHelper.showMessage(errMsg)
        }

        return result
    }

    private fun updateEditTextsUI() {
        binding.apply {
            etStartDate.text = viewModel?.eventModel?.startTime?.toLocalDateTime()?.toFormatString("MMMM dd, yyyy")
            etEndDate.text = viewModel?.eventModel?.endTime?.toLocalDateTime()?.toFormatString("MMMM dd, yyyy")
            etStartTime.text = viewModel?.eventModel?.startTime?.toLocalDateTime()?.toFormatString("hh:mm a")
            etEndTime.text = viewModel?.eventModel?.endTime?.toLocalDateTime()?.toFormatString("hh:mm a")
        }
    }

    private fun changeLocation(place: PlaceModel?) {
        viewModel.eventModel.address = place?.formattedAddress ?: place?.address
        viewModel.eventModel.locationName = place?.name
        viewModel.eventModel.lat = place?.latitude
        viewModel.eventModel.long = place?.longitude
        updateLocationUI()
    }

    private fun updateLocationUI() {
        // Location Boundaries
        adapterBoundary.updateAdapter(viewModel.boundaries, this, viewModel.selectedBoundaryItem)

        binding.apply {
            etLocation.text = if (!viewModel?.eventModel?.locationName.isNullOrEmpty()) viewModel?.eventModel?.locationName else viewModel?.eventModel?.address

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

    private fun showDatePicker(editText: PlansEditTextView?) {
        selectedEditText = editText
        var minDate = Date()
        val startTime = viewModel.eventModel.startTime?.toDate() ?: Date(Date().time + 60 * 1000)
        when (editText) {
            binding.etStartDate -> {
                minDate = Date(Date().time + 60 * 1000)
                calendar.time = if (startTime.time >= minDate.time) startTime else minDate
            }
            binding.etEndDate -> {
                val endTime = viewModel.eventModel.endTime?.toDate() ?: Date(startTime.time + 30 * 60 * 1000)
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

    //********************************* PlansEditTextViewListener **************************************//
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
                    gotoLocationSelection(LocationSelectionVM.LocationSelectionType.CREATE_EVENT)
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
                btnContinue -> {
                    val action = NavDashboardDirections.actionGlobalCreateEventProgress2Fragment()
                    navigate(directions = action)
                }
            }
        }
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
                    updateContinueUI()
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
                    updateContinueUI()
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
                        updateContinueUI()
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
                        updateContinueUI()
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