package com.h2Invent.skibin

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CheckinDialog : AppCompatDialogFragment() {
    var onConfirmed: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog, null)
        val isError = requireArguments().getBoolean(ARG_IS_ERROR)
        view.findViewById<TextView>(R.id.dialog_name).text = requireArguments().getString(ARG_NAME).orEmpty()
        view.findViewById<TextView>(R.id.dialog_kurs).text = requireArguments().getString(ARG_KURS).orEmpty()
        view.findViewById<TextView>(R.id.dialog_text).text = requireArguments().getString(ARG_TEXT).orEmpty()
        view.findViewById<TextView>(R.id.dialog_symbol).text = if (isError) "×" else "✓"
        view.findViewById<TextView>(R.id.dialog_title).setText(
            if (isError) R.string.checkinRejectedTitle else R.string.checkinSuccessTitle,
        )
        view.findViewById<TextView>(R.id.dialog_state).apply {
            setText(
                if (isError) R.string.checkinRejectedState else R.string.checkinSuccessState,
            )
            setTextColor(
                ContextCompat.getColor(requireContext(), if (isError) R.color.buttonDanger else R.color.phoneGreen),
            )
        }
        view.findViewById<View>(R.id.dialog_symbol).setBackgroundResource(
            if (isError) R.drawable.bg_checkin_error_symbol else R.drawable.bg_checkin_success_symbol,
        )
        view.setBackgroundResource(
            if (isError) R.drawable.bg_checkin_error_dialog else R.drawable.bg_checkin_success_dialog,
        )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()
        view.findViewById<Button>(R.id.dialog_ok).setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        onConfirmed?.invoke()
        onConfirmed = null
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        onConfirmed = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_NAME = "arg_name"
        private const val ARG_KURS = "arg_kurs"
        private const val ARG_TEXT = "arg_text"
        private const val ARG_IS_ERROR = "arg_is_error"

        fun newInstance(name: String, kurs: String, text: String, isError: Boolean): CheckinDialog {
            return CheckinDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_KURS, kurs)
                    putString(ARG_TEXT, text)
                    putBoolean(ARG_IS_ERROR, isError)
                }
            }
        }
    }
}
