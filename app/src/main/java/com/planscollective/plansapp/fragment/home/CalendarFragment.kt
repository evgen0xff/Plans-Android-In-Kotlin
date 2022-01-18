package com.planscollective.plansapp.fragment.home

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.CalendarEventsAdapter
import com.planscollective.plansapp.customUI.PlansDotSpan
import com.planscollective.plansapp.databinding.FragmentCalendarBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toLocalDate
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.models.viewModels.CalendarVM
import com.prolificinteractive.materialcalendarview.*
import org.threeten.bp.LocalDate


class CalendarFragment : PlansBaseFragment<FragmentCalendarBinding>(),
    OnMonthChangedListener,
    OnDateSelectedListener
{

    private val viewModel: CalendarVM by viewModels()
    private var decoratorEventDay = EventDayDecorator()
    private var adapterEvents = CalendarEventsAdapter()
    private val args: CalendarFragmentArgs by navArgs()
    override var screenName: String? = "Calendar_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        viewModel.selectedDate.observe(viewLifecycleOwner, {
            refreshAll(false)
        })
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            updateUI()
        })
        viewModel.actionEnterKey = actionEnterKey
        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Search
        binding.etSearch.addTextChangedListener {
            viewModel.searchEvents()
        }

        // Calendar View
        binding.calendarView.apply {
            removeDecorators()
            tileWidth = (OSHelper.widthScreen / 7.0).toInt()

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.selector_calendar_day)
            val drawableBackground = ContextCompat.getDrawable(requireContext(), R.drawable.circle_white_fill_opacity)
            addDecorators(TodayDecorator(drawableBackground, CalendarDay.today()), SelectionDecorator(drawable), decoratorEventDay)
        }

        // Event List
        val layoutMangerList = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutMangerList
        binding.recyclerView.adapter = adapterEvents

        // Empty UI
        binding.layoutEmpty.visibility = View.GONE
        binding.btnCreateEvent.setOnSingleClickListener(this)

        // Set current day
        if (viewModel.selectedDate.value == null) {
            val selectedDay = CalendarDay.from(args.eventModel?.startTime?.toLocalDate() ?: LocalDate.now())
            binding.calendarView.currentDate = selectedDay
            binding.calendarView.selectedDate = selectedDay
            viewModel.selectedDate.value = selectedDay
        }else {
            binding.calendarView.currentDate = viewModel.selectedDate.value
            binding.calendarView.selectedDate = viewModel.selectedDate.value
            refreshAll(false)
        }
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    override fun onResume() {
        super.onResume()
        binding.calendarView.also {
            it.setOnMonthChangedListener(this)
            it.setOnDateChangedListener(this)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.calendarView.also {
            it.setOnMonthChangedListener(null)
            it.setOnDateChangedListener(null)
        }
    }

    private fun updateUI() {
        //Set viewmodel to UI data binding
        binding.viewModel = viewModel

        // Calendar View
        decoratorEventDay.daysEvent = viewModel.listDaysOfEvents
        binding.calendarView.invalidateDecorators()

        // Events RecyclerView
        adapterEvents.updateAdapter(viewModel.listSearchedEvents, this, viewModel.selectedDate.value)

        // Empty UI
        binding.layoutEmpty.visibility = if (viewModel.listSearchedEvents.size > 0) View.GONE else View.VISIBLE
    }

    inner class TodayDecorator(var drawable: Drawable? = null, var today: CalendarDay? = null) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == today
        override fun decorate(view: DayViewFacade) {
            drawable?.let {
                view.setBackgroundDrawable(it)
            }
        }
    }

    inner class SelectionDecorator(var drawable: Drawable? = null) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = true
        override fun decorate(view: DayViewFacade) {
            drawable?.let {
                view.setSelectionDrawable(it)
            }
        }
    }

    inner class EventDayDecorator(var daysEvent: ArrayList<CalendarDay>? = null) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return daysEvent?.contains(day) ?: false
        }
        override fun decorate(view: DayViewFacade) {
            view.addSpan(PlansDotSpan(2.toPx().toFloat(), Color.parseColor("#FFFFFF")))
        }
    }

    //****************************** Calendar View Listener ******************************//
    override fun onMonthChanged(widget: MaterialCalendarView?, date: CalendarDay?) {
        binding.calendarView.selectedDate = date
        viewModel.selectedDate.value = date
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        viewModel.selectedDate.value = date
    }

    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.btnCreateEvent ->{
                gotoCreateEvent()
            }
        }
    }


}