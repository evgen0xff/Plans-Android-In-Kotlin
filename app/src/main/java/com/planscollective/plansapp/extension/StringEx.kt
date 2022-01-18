package com.planscollective.plansapp.extension

import android.content.Context
import android.graphics.Typeface
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.PatternsCompat
import com.planscollective.plansapp.manager.UserInfo
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern





fun String.toDate(pattern: String) : Date? {
    return SimpleDateFormat(pattern).parse(this)
}

fun String.isEmailValid(): Boolean {
    return PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isPasswordValid(): Boolean {
    val expression = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,16}"
    val inputStr: CharSequence = this
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(inputStr)

    return matcher.matches()
}

fun String.isMobileValid(lengthMin: Int = 10): Boolean {
    val expression = "^[0-9]{$lengthMin,${lengthMin + 3}}$"
    val inputStr: CharSequence = this
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(inputStr)

    return matcher.matches()
}

fun String.isValidNumber(context: Context?): Boolean {
    val ctx = context ?: false
    return try {
        val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
        val nameCode = getCountryIsoCode(context)
        val phoneNumber = phoneNumberUtil.parse(this, nameCode)
        phoneNumberUtil.isValidNumber(phoneNumber)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun String.formatPhoneNumber(
    context: Context? = null,
    numberFormat: PhoneNumberUtil.PhoneNumberFormat? = null,
    separator: String? = null,
    isRemovedOwnCountryCode: Boolean = false,
    countryNameCode: String? = null,
): String {
    var result = this
    try {
        val nameCode = countryNameCode ?: getCountryIsoCode(context)
        result = if (context == null) {
            PhoneNumberUtils.formatNumber(this, nameCode?.uppercase())
        }else {
            val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
            val format = numberFormat ?: PhoneNumberUtil.PhoneNumberFormat.NATIONAL
            val phoneNumber = phoneNumberUtil.parse(this, nameCode)
            var isAddedCountryCode = format != PhoneNumberUtil.PhoneNumberFormat.NATIONAL
            var formatted = phoneNumberUtil.format(phoneNumber, format)

            if (isRemovedOwnCountryCode) {
                val nameCodeOwn = (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.networkCountryIso?.uppercase()
                if (nameCode == nameCodeOwn) {
                    isAddedCountryCode = false
                    formatted = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                    if (numberFormat == PhoneNumberUtil.PhoneNumberFormat.E164) {
                        formatted = phoneNumber.nationalNumber.toString()
                    }
                }
            }

            separator?.also {
                val countryCode = if (isAddedCountryCode) "+${phoneNumber.countryCode} " else ""
                var nationalNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                nationalNumber = nationalNumber.replace(" ", it)
                .replace("(", "")
                .replace(")", "")
                .replace("-", it)

                formatted = countryCode + nationalNumber
            }
            formatted ?: this
        }
    }catch (e : Exception){
        e.printStackTrace()
    }
    return result
}

fun String.getCountryIsoCode(context: Context?): String? {
    val ctx = context ?: return null

    val validateNumber = if (this.startsWith("+")) this else {
        return  (ctx.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.networkCountryIso?.uppercase()
    }

    val phoneNumberUtil = PhoneNumberUtil.createInstance(ctx)
    val phoneNumber = try {
        phoneNumberUtil.parse(validateNumber, null)
    }catch (e : NumberParseException) {
        null
    } ?: return null

    return phoneNumberUtil.getRegionCodeForCountryCode(phoneNumber.countryCode)
}

/*
    Get an example phone number from phone number code -> this
    phoneCode: Phone Number Code -> "+1", "+86"
    countryNameCode : Country Name Code -> "US", "CN"....
 */
fun String.getExamplePhoneNumber(context: Context, countryNameCode: String) : String? {
    var formattedNumber: String? = ""
    val countryNameCode = countryNameCode.uppercase()
    val phoneUtil = PhoneNumberUtil.createInstance(context)
    val exampleNumber = phoneUtil.getExampleNumberForType(countryNameCode, PhoneNumberUtil.PhoneNumberType.MOBILE)

    if (exampleNumber != null) {
        formattedNumber = exampleNumber.nationalNumber.toString() + ""
        formattedNumber = "$this$formattedNumber".formatPhoneNumber(countryNameCode = countryNameCode)
        if (formattedNumber != null) {
            formattedNumber = formattedNumber.substring("$this".length).trim()
        }
    }
    return formattedNumber
}

fun String.getMaskForMobile(charMask: String = "#", countryNameCode: String, context: Context, isBracket: Boolean = false) : String {
    var result = "$charMask$charMask$charMask-$charMask$charMask$charMask-$charMask$charMask$charMask$charMask"
    getExamplePhoneNumber(context, countryNameCode)?.apply {
        var list: List<String>? = null
        val formatted = replace("(", "").replace(")", "")
        if (formatted.contains("-")) {
            list = formatted.split("-")
        }else if (formatted.contains(" ")){
            list = formatted.split(" ")
        }

        if (list != null) {
            result = ""
            for (i in list.indices) {
                if (isBracket) result += "["

                for (j in 1..list[i].length){
                    result += charMask
                }

                if (isBracket) result += "]"

                if (i < list.lastIndex) {
                    result += "-"
                }
            }
        }

    }
    return result
}

fun CharSequence.getHeight(
    context: Context?,
    textSize: Int,
    deviceWidth: Int,
    fontId: Int?,
    padding: Int = 0
): Int {
    val font = if (context != null && fontId != null) ResourcesCompat.getFont(context, fontId) else null
    return getHeight(context, textSize, deviceWidth, font, padding)
}

fun CharSequence.getHeight(
    context: Context?,
    textSize: Int,
    deviceWidth: Int,
    typeface: Typeface?,
    padding: Int = 0
): Int {
    val textView = TextView(context)
    textView.setPadding(padding, 0, padding, padding)
    textView.typeface = typeface
    textView.setText(this, TextView.BufferType.SPANNABLE)
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
    val widthMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
    val heightMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    textView.measure(widthMeasureSpec, heightMeasureSpec)
    return textView.measuredHeight
}

fun String.removeOwnCountry(countryOwn: String? = null) : String {
    var result = this
    (countryOwn ?: UserInfo.countryOwn)?.takeIf { it.isNotEmpty() }?.also {
        val components = split(", ").toArrayList()
        components.lastIndexOf(it).takeIf{index -> index >= 0}?.also { index ->
            val list = if (index > 0) {
                components.dropLast(components.size - index)
            }else {
                components.drop(1)
            }
            result = list.joinToString(", ")
        }
    }
    return result
}

fun String.getCountryNameFromAddress() : String? {
    return split(", ").lastOrNull { !it.containNumbers() }
}

fun String.removeZipCode(): String {
    var list = split(", ")
    list.indexOfLast { it.containNumbers() }.takeIf { it > 1 }?.also {
        list = list.dropLast(list.size - it)
    }

    return list.joinToString(", ")
}

fun String.containNumbers() : Boolean {
    return matches(".*\\d.*".toRegex())
}

// url = file path or whatever suitable URL you want.
fun String.getMimeType(): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(this)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}
