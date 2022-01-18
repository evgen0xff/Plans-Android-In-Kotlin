package com.planscollective.plansapp.customUI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.ViewPlansEdittextBinding
import com.planscollective.plansapp.extension.setLayoutHeight
import com.planscollective.plansapp.extension.setLayoutWidth
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx

interface PlansEditTextViewListener {
    fun didChangedText(text: String?, editText: PlansEditTextView?){}
    fun didFocusChanged(editText: PlansEditTextView?, hasFocus: Boolean){}
    fun didClicked(editText: PlansEditTextView?){}
    fun didClickedClearBtn(editText: PlansEditTextView?) : Boolean { return false }
    fun didClickedAction(editText: PlansEditTextView?){}
}

class PlansEditTextView : LinearLayout, TextWatcher {

    enum class Style {
        WHITE {
            override val colorMain = Color.WHITE
            override val colorTextFocused = Color.WHITE
            override val colorText = Color.WHITE
            override val colorHintTextFocused = Color.WHITE
            override val colorHintText = PlansColor.WHITE_OPACITY50
            override val colorBottomFocused = Color.WHITE
            override val colorBottom = PlansColor.WHITE_OPACITY50
        },
        PURPLE{
            override val colorMain = PlansColor.PURPLE_JOIN
            override val colorTextFocused = Color.BLACK
            override val colorText = PlansColor.GRAY_LABEL
            override val colorHintTextFocused = PlansColor.PURPLE_JOIN
            override val colorHintText = Color.BLACK
            override val colorBottomFocused = PlansColor.PURPLE_JOIN
            override val colorBottom = PlansColor.GRAY_LABEL
        };

        abstract val colorMain: Int
        abstract val colorTextFocused: Int
        abstract val colorText: Int
        abstract val colorHintTextFocused: Int
        abstract val colorHintText: Int
        abstract val colorBottomFocused: Int
        abstract val colorBottom: Int
    }

    var binding: ViewPlansEdittextBinding? = null

    var tvHint: TextView? = null
    var editTextPurple: EditText? = null
    var editTextWhite: EditText? = null
    var btnValid: ImageView? = null
    var btnClear: ImageView? = null
    var viewBottom: View? = null
    var tvAction: TextView? = null

    val editText: EditText?
        get() = editTextPurple?.takeIf { it.visibility == VISIBLE } ?: editTextWhite

    var listener: PlansEditTextViewListener? = null

    var isFocusableForEdit = true
        set(value) {
            field = value
            editText?.isClickable = value
            editText?.isCursorVisible = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editText?.focusable = if (value) View.FOCUSABLE else View.NOT_FOCUSABLE
            }
            editText?.isFocusableInTouchMode = value
        }

    var enableAction = false
        set(value) {
            field = value
            tvAction?.visibility = if (value) View.VISIBLE else View.GONE
        }

    var enableClear = true
        set(value) {
            field = value
            btnClear?.visibility = if (value && editText?.hasFocus() == true && editText?.text.toString().trim().isNotEmpty()) View.VISIBLE else View.GONE
        }

    var enableValid = false
        set(value) {
            field = value
            btnValid?.visibility = if (value) View.VISIBLE else View.GONE
        }

    var enableFloatingHint = true
        set(value) {
            field = value
            tvHint?.visibility = if (value) {
                if (editText?.hasFocus() == true || editText?.text.toString().trim().isNotEmpty())  View.VISIBLE else View.INVISIBLE
            } else View.GONE
        }

    var text : String? = null
        get() = editText?.text?.toString()
        set(value) {
            field = value
            editText?.setText(value)
            updateUI()
        }

    var inputType : Int = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        get() = editText?.inputType ?: InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        set(value) {
            field = value
            value.also { editText?.inputType = it }
        }

    var hintText: String?
        get() = tvHint?.text?.toString()
        set(value) {
            tvHint?.text = value
            editText?.hint = value
            updateUI()
        }

    var hintTextColor : Int = PlansColor.BLACK
        set(value) {
            field = value
            tvHint?.setTextColor(value)
        }

    var textColor: Int = PlansColor.BLACK
        set(value) {
            field = value
            editText?.setTextColor(value)
        }

    var bottomColor: Int = PlansColor.GRAY_LABEL
        set(value) {
            field = value
            viewBottom?.setBackgroundColor(value)
        }

    var iconValid: Drawable? = null
        set(value) {
            field = value
            value?.also { btnValid?.setImageDrawable(it) }
        }
    var widthValidIcon: Int? = null
        set(value) {
            field = value
            value?.also { btnValid?.setLayoutWidth(it) }
        }

    var style: Style = Style.PURPLE
        set(value) {
            field = value
            updateTheme()
        }

    var textAction: String = "Save"
        set(value) {
            field = value
            tvAction?.text = value
        }


    constructor(context: Context?) : super(context) {
        onInit(context)
        setupListeners()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        onInit(context)
        setupUI(context, attrs)
        setupListeners()
    }

    private fun onInit(context: Context?) {
        binding = ViewPlansEdittextBinding.inflate(LayoutInflater.from(context), this, true)
        tvHint = binding?.tvHint
        btnValid = binding?.btnValid
        btnClear = binding?.btnClear
        viewBottom = binding?.viewBottom
        editTextPurple = binding?.editTextPurple
        editTextWhite = binding?.editTextWhite
        tvAction = binding?.tvAction
    }

    @SuppressLint("Recycle")
    private fun setupUI(context: Context?, attrs: AttributeSet?) {
        context?.obtainStyledAttributes(attrs, R.styleable.PlansEditTextView)?.also { typedArray ->
            style = typedArray.getInt(R.styleable.PlansEditTextView_style, Style.PURPLE.ordinal).let {
                Style.values().firstOrNull { item -> item.ordinal == it } ?: Style.PURPLE
            }
            enableAction = typedArray.getBoolean(R.styleable.PlansEditTextView_enableAction,false)
            enableClear = typedArray.getBoolean(R.styleable.PlansEditTextView_enableClear,true)
            enableValid = typedArray.getBoolean(R.styleable.PlansEditTextView_enableValid,false)
            enableFloatingHint = typedArray.getBoolean(R.styleable.PlansEditTextView_enableFloatingHint,true)
            inputType = typedArray.getInt(R.styleable.PlansEditTextView_inputTypePlans, InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
            isFocusableForEdit = typedArray.getBoolean(R.styleable.PlansEditTextView_isFocusableForEdit,true)
            iconValid = typedArray.getDrawable(R.styleable.PlansEditTextView_validIcon)
            widthValidIcon = typedArray.getLayoutDimension(R.styleable.PlansEditTextView_widthValidIcon, 16.toPx())
            text = typedArray.getString(R.styleable.PlansEditTextView_text)
            hintText = typedArray.getString(R.styleable.PlansEditTextView_hintText)
            textAction = typedArray.getString(R.styleable.PlansEditTextView_textAction) ?: "Save"

            hintTextColor = typedArray.getColor(R.styleable.PlansEditTextView_hintTextColor, style.colorHintText)
            textColor = typedArray.getColor(R.styleable.PlansEditTextView_textColor, style.colorText)
            bottomColor = typedArray.getColor(R.styleable.PlansEditTextView_bottomColor, style.colorBottom)
        }
    }

    private fun setupListeners() {
        btnClear?.setOnSingleClickListener {
            listener?.didClickedClearBtn(this)?.apply {
                if (!this) text = ""
            } ?: run {
                text = ""
            }
        }

        editText?.setOnFocusChangeListener { v, hasFocus ->
            updateUI()
            listener?.also { it.didFocusChanged(this, hasFocus) }
        }
        editText?.setOnSingleClickListener{
            listener?.also { it.didClicked(this) }
        }

        tvAction?.setOnSingleClickListener {
            listener?.also { it.didClickedAction(this) }
        }
    }

    private fun updateTheme() {
        when(style) {
            Style.PURPLE -> {
                editTextWhite?.visibility = View.GONE
                editTextPurple?.visibility = View.VISIBLE
                tvAction?.setTextColor(style.colorMain)
                btnClear?.setImageResource(R.drawable.ic_x_grey)
            }
            Style.WHITE -> {
                editTextWhite?.visibility = View.VISIBLE
                editTextPurple?.visibility = View.GONE
                tvAction?.setTextColor(style.colorMain)
                btnClear?.setImageResource(R.drawable.ic_x_white)
            }
        }
    }

    private fun updateUI() {
        btnClear?.visibility = if (enableClear && editText?.hasFocus() == true && editText?.text.toString().trim().isNotEmpty()) View.VISIBLE else View.GONE

        tvHint?.visibility = if (enableFloatingHint) {
            if (editText?.hasFocus() == true || editText?.text.toString().trim().isNotEmpty()){
                editText?.hint = ""
                View.VISIBLE
            } else{
                editText?.hint = hintText
                View.INVISIBLE
            }
        }else View.GONE

        hintTextColor = if (editText?.hasFocus() == true) {
            textColor = style.colorTextFocused
            bottomColor = style.colorBottomFocused
            viewBottom?.setLayoutHeight(2.toPx())
            style.colorHintTextFocused
        }else {
            textColor = style.colorText
            bottomColor = style.colorBottom
            viewBottom?.setLayoutHeight(1.toPx())
            style.colorHintText
        }


    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        if (s?.hashCode() == editText?.text?.hashCode()) {
            updateUI()
            listener?.also { it.didChangedText(editText?.text?.toString(), this) }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        editText?.addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        editText?.removeTextChangedListener(this)
    }

}