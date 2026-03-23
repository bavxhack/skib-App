package com.h2Invent.skibin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class FragmentConnection : Fragment() {
    private var listener: OnItemSelectedListener? = null

    private lateinit var userInfo: LinearLayout
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userOrg: TextView
    private lateinit var saveUser: MaterialButton
    private lateinit var resetUser: MaterialButton
    private lateinit var userEmailLayout: LinearLayout
    private lateinit var userEmailCode: EditText
    private lateinit var userEmailButton: MaterialButton
    private lateinit var scanConnection: MaterialButton

    private lateinit var orgSave: MaterialButton
    private lateinit var orgReset: MaterialButton
    private lateinit var orgInfo: LinearLayout
    private lateinit var orgName: TextView
    private lateinit var orgPartner: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnItemSelectedListener
            ?: error("Parent activity must implement OnItemSelectedListener")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanConnection = view.findViewById(R.id.scanConnection)
        userInfo = view.findViewById(R.id.user_info)
        userName = view.findViewById(R.id.user_name)
        userEmail = view.findViewById(R.id.user_email)
        userOrg = view.findViewById(R.id.user_organisation)
        saveUser = view.findViewById(R.id.saveUserConnection)
        resetUser = view.findViewById(R.id.resetUserConnection)
        userEmailLayout = view.findViewById(R.id.userEmailCOnfirmation)
        userEmailCode = view.findViewById(R.id.edituseremailCode)
        userEmailButton = view.findViewById(R.id.buttonEmailCOnfirmation)
        orgSave = view.findViewById(R.id.connectionOrgSave)
        orgReset = view.findViewById(R.id.connectionResetOrg)
        orgInfo = view.findViewById(R.id.orgInfo)
        orgName = view.findViewById(R.id.org_name)
        orgPartner = view.findViewById(R.id.orgPartner)

        scanConnection.setOnClickListener { listener?.scanClicked() }
        saveUser.setOnClickListener { listener?.userSaveClicked() }
        resetUser.setOnClickListener { listener?.resetClicked() }
        orgSave.setOnClickListener { listener?.orgSaveClicked() }
        orgReset.setOnClickListener { listener?.resetClicked() }
        userEmailButton.setOnClickListener { listener?.userEmailConfirmClicked(userEmailCode.text?.toString().orEmpty()) }

        listener?.init()
    }

    fun setUserText(name: String, organisation: String, email: String) {
        userName.text = name
        userOrg.text = organisation
        userEmail.text = email
        showUserInfo(true)
    }

    fun setOrgText(name: String, partner: String) {
        orgName.text = name
        orgPartner.text = partner
        showOrgInfo(true)
    }

    fun activateEmail(visible: Boolean) {
        userEmailLayout.visibility = if (visible) View.VISIBLE else View.GONE
        scanConnection.isEnabled = !visible
        if (visible) scanConnection.text = getString(R.string.emailCheckHint)
    }

    fun setScanText(text: String) {
        scanConnection.text = text
    }

    fun enableOrgSetting(active: Boolean) {
        orgSave.isEnabled = active
        orgSave.visibility = if (active) View.VISIBLE else View.GONE
        orgReset.isEnabled = active
        orgReset.visibility = if (active) View.VISIBLE else View.GONE
    }

    fun enableUserSetting(active: Boolean) {
        saveUser.isEnabled = active
        saveUser.visibility = if (active) View.VISIBLE else View.GONE
        resetUser.isEnabled = active
        resetUser.visibility = if (active) View.VISIBLE else View.GONE
    }

    fun showUserInfo(visible: Boolean) {
        userInfo.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun showOrgInfo(visible: Boolean) {
        orgInfo.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun enableScanButton(enabled: Boolean) {
        scanConnection.isEnabled = enabled
    }

    interface OnItemSelectedListener {
        fun scanClicked()
        fun userSaveClicked()
        fun orgSaveClicked()
        fun resetClicked()
        fun userEmailConfirmClicked(token: String)
        fun init()
    }

    companion object {
        const val TITLE = "Verbindung"
        fun newInstance() = FragmentConnection()
    }
}
