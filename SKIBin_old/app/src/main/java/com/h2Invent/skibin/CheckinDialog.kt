package com.h2Invent.skibin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CheckinDialog : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog, null)
        view.findViewById<TextView>(R.id.dialog_name).text = requireArguments().getString(ARG_NAME).orEmpty()
        view.findViewById<TextView>(R.id.dialog_kurs).text = requireArguments().getString(ARG_KURS).orEmpty()
        view.findViewById<TextView>(R.id.dialog_text).text = requireArguments().getString(ARG_TEXT).orEmpty()
        view.setBackgroundResource(
            if (requireArguments().getBoolean(ARG_IS_ERROR)) R.color.backgroundError else R.color.backgroundSuccess,
        )

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setNegativeButton(R.string.OkText, null)
            .create()
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
