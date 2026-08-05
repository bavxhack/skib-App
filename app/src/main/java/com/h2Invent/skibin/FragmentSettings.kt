package com.h2Invent.skibin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {
    private var listener: OnItemSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnItemSelectedListener
            ?: error("Parent activity must implement OnItemSelectedListener")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    interface OnItemSelectedListener

    companion object {
        const val TITLE = "Einstellungen"
        fun newInstance() = FragmentSettings()
    }
}
