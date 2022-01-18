package com.planscollective.plansapp.customUI

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.extension.timeAgoSince
import com.planscollective.plansapp.extension.toLocalDateTime
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel

class NotificationActivityTextView: AppCompatTextView/*, ViewTreeObserver.OnGlobalLayoutListener*/ {

    private val INVALID_END_INDEX = -1
    private val ELLIPSIZE = "... "


    var notify: NotificationActivityModel? = null
    var lineEndIndex = 0
    var trimLines = 3

    private var timeAgo: CharSequence? = null


    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){
        updateNotify()
    }

    fun updateNotify(notification: NotificationActivityModel? = null) {
        notification?.also {
            notify = it
            maxLines = if (checkIfNeededAdjustText()) 3 else Integer.MAX_VALUE
            text = notify?.getSpannableText(withAgo = false)
            post { adjustDisplayText() }
        }
    }

    private fun checkIfNeededAdjustText(): Boolean {
        return when(notify?.notificationType) {
            "Comment Like", "Like", "Comment" -> true
            else -> false
        }
    }

    private fun adjustDisplayText() {
        val countLines = lineCount
        if (checkIfNeededAdjustText()) {
            refreshLineEndIndex()
            updateText()
        }else {
            text = notify?.getSpannableText()
            post {
                if (countLines != lineCount) {
                    text = notify?.getSpannableText(breakLine = true)
                }
            }
        }
    }

    private fun refreshLineEndIndex() {
        try {
            if (trimLines == 0) {
                lineEndIndex = layout.getLineEnd(0)
            } else if (trimLines in 1..lineCount) {
                lineEndIndex = layout.getLineEnd(trimLines - 1)
            } else {
                lineEndIndex = INVALID_END_INDEX
            }
        } catch (ignored: Exception) {
        }
    }

    private fun updateText() {
        notify?.createdAtTimestamp?.toLocalDateTime()?.timeAgoSince()?.also {
            timeAgo = it
        }
        text = getDisplayableText()
    }

    private fun getDisplayableText(): CharSequence? {
        var spannableBuilder = notify?.getSpannableText()
        val trimEndIndex = lineEndIndex - (ELLIPSIZE.length + (timeAgo?.length ?: 0))
        if (trimEndIndex > 0) {
            spannableBuilder = SpannableStringBuilder(text, 0, trimEndIndex).append(ELLIPSIZE)
            val start = spannableBuilder.length
            spannableBuilder.append(timeAgo, AbsoluteSizeSpan(13, true), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableBuilder.setSpan(ForegroundColorSpan(PlansColor.GRAY_SECOND), start, spannableBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableBuilder
    }

}
