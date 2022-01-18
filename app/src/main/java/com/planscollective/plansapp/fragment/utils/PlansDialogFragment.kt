package com.planscollective.plansapp.fragment.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.DialogFragment
import com.planscollective.plansapp.databinding.FragmentPlansDialogBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage

class PlansDialogFragment(
    private var message: String?,
    private var titleYes: String? = null,
    private var titleNo: String? = null,
    private var urlImage: String? = null,
    private var data: Any? = null,
    private var attributedMsg: String? = null,
    private var actionComplete: ((data: Any?) -> Unit)? = null,
    private var actionNo: ((data: Any?) -> Unit)? = null,
    private var actionYes: ((data: Any?) -> Unit)? = null,
) : DialogFragment() {

    companion object {
        const val TAG = "PlansDialogFragment"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_MESSAGE = "KEY_MESSAGE"
        private const val KEY_IMAGE_URL = "KEY_IMAGE_URL"

        fun getInstance (title: String? = null,
                         message: String? = null,
                         urlImage: String? = null
        ): PlansDialogFragment {
            val args = Bundle()
            title?.let {
                args.putString(KEY_TITLE, title)
            }
            message?.let {
                args.putString(KEY_MESSAGE, message)
            }
            urlImage?.let {
                args.putString(KEY_IMAGE_URL, urlImage)
            }
            val fragment = PlansDialogFragment(message)
            fragment.arguments = args
            return fragment
        }
    }

    private var binding: FragmentPlansDialogBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)

        binding = FragmentPlansDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
        setupOnClickListener(view)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupView(view: View) {
        binding?.apply {
            urlImage?.let {
                imageView.visibility = View.VISIBLE
                imageView.setUserImage(it)
            } ?: run {
                imageView.visibility = View.GONE
            }

            tvMessage.text = if (!attributedMsg.isNullOrEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Html.fromHtml(attributedMsg, Html.FROM_HTML_MODE_LEGACY)
                else
                    Html.fromHtml(attributedMsg)
            }else {
                 message
            }

            tvCancel.text = titleNo ?: "CANCEL"
            tvYes.text = titleYes ?: "YES"
        }
    }

    private fun setupOnClickListener (view: View) {
        binding?.apply {
            btnCancel.setOnSingleClickListener {
                actionNo?.also { it(data)}
                actionComplete?.also { it(data) }
                dismiss()
            }
            btnYes.setOnSingleClickListener {
                actionYes?.also { it(data) }
                actionComplete?.also { it(data) }
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}