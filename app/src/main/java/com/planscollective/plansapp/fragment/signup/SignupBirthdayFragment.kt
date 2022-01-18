package com.planscollective.plansapp.fragment.signup

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.databinding.FragmentSignupBirthdayBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toFormatString
import com.planscollective.plansapp.extension.toSeconds
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import java.util.*

class SignupBirthdayFragment : AuthBaseFragment<FragmentSignupBirthdayBinding>(),
    OnSingleClickListener, DatePickerDialog.OnDateSetListener {

    private val calendar = Calendar.getInstance()
    private var selectedDate : Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBirthdayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initializeData() {
        super.initializeData()
        calendar.add(Calendar.YEAR, -13)
    }

    override fun setupUI() {
        super.setupUI()

        // Back Button
        binding.btnBack.setOnSingleClickListener (this)

        // Birthday EditView
        binding.etMonth.setOnSingleClickListener(this)
        binding.etDay.setOnSingleClickListener(this)
        binding.etYear.setOnSingleClickListener(this)

        // Continue Button
        binding.tvContinue.setOnSingleClickListener(this)

        selectedDate = authVM.user.value?.birthDay

        updateUI()
    }

    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                etMonth, etDay,  etYear -> {
                    showDatePicker()
                }
                tvContinue -> {
                    actionContinue()
                }
                btnBack -> {
                    gotoBack()
                }
            }
        }
    }

    private fun updateUI() {
        binding.tvContinue.apply {
            isClickable = if (selectedDate != null) {
                binding.etMonth.setText(selectedDate?.toFormatString("MM"))
                binding.etDay.setText(selectedDate?.toFormatString("dd"))
                binding.etYear.setText(selectedDate?.toFormatString("yyyy"))
                setBackgroundResource(R.drawable.button_bkgnd_white)
                setTextColor(resources.getColor(R.color.pink))
                true
            }else {
                binding.etMonth.setText("")
                binding.etDay.setText("")
                binding.etYear.setText("")
                setBackgroundResource(R.drawable.button_bkgnd_gray)
                setTextColor(resources.getColor(R.color.white))
                false
            }
        }

    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDlg = DatePickerDialog(requireActivity(), this, year, month, day)
        datePickerDlg.datePicker.maxDate = calendar.timeInMillis
        datePickerDlg.show()
        datePickerDlg.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.pink))
        datePickerDlg.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.pink))

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        selectedDate = calendar.time
        updateUI()
    }


    private fun actionContinue() {
        hideKeyboard()
        authVM.user.value?.birthDay = selectedDate
        authVM.user.value?.dob = (selectedDate?.time ?: 0).toSeconds()

        PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
    }


}