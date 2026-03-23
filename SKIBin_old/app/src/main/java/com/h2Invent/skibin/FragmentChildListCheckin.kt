package com.h2Invent.skibin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jaredrummler.materialspinner.MaterialSpinner
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentChildListCheckin : Fragment(), ChildListAdapter.OnItemClickListener {
    private var itemSelectedListener: OnItemSelectedListener? = null
    private val requestQueue by lazy { Volley.newRequestQueue(requireContext()) }

    private lateinit var titleView: TextView
    private lateinit var spinner: MaterialSpinner
    private lateinit var recyclerView: RecyclerView

    private var childListUrl: String = ""
    private var userToken: String = ""
    private val allChildren = mutableListOf<ChildListItem>()
    private val visibleChildren = mutableListOf<ChildListItem>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        itemSelectedListener = context as? OnItemSelectedListener
            ?: error("Parent activity must implement OnItemSelectedListener")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_checkinlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView = view.findViewById(R.id.url)
        spinner = view.findViewById(R.id.schulenChooseCheckin)
        recyclerView = view.findViewById(R.id.recyclerCheckinlIst)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setChildCount(0)
        if (childListUrl.isNotBlank() && userToken.isNotBlank()) parseJson()
    }

    fun setConfig(url: String, token: String) {
        childListUrl = url
        userToken = token
        if (view != null && url.isNotBlank() && token.isNotBlank()) parseJson()
    }

    override fun onItemClick(position: Int) {
        val selectedChild = visibleChildren.getOrNull(position) ?: return
        startActivity(
            Intent(requireContext(), ChildDetailActivity::class.java)
                .putExtra(ChildDetailActivity.EXTRA_URL, selectedChild.detailUrl)
                .putExtra(ChildListMainActivity.EXTRA_USER_TOKEN, userToken),
        )
    }

    override fun onCheckinClick(position: Int) = Unit

    private fun parseJson() {
        val uri = "$childListUrl?communicationToken=$userToken"
        val request = JsonObjectRequest(Request.Method.GET, uri, null,
            { response -> handleResponse(response) },
            { error -> Toast.makeText(context, error.localizedMessage ?: "Request failed", Toast.LENGTH_LONG).show() },
        )
        requestQueue.add(request)
    }

    private fun handleResponse(response: JSONObject) {
        if (response.optBoolean("error")) {
            Toast.makeText(context, response.optString("errorText"), Toast.LENGTH_LONG).show()
            return
        }

        allChildren.clear()
        visibleChildren.clear()

        val number = response.optInt("number")
        setChildCount(number)
        if (number == 0) {
            allChildren += emptyPlaceholder()
            bindChildren(allChildren)
            return
        }

        val result = response.optJSONArray("result")
        for (index in 0 until (result?.length() ?: 0)) {
            val child = result?.optJSONObject(index) ?: continue
            allChildren += child.toChildListItem()
        }

        val schools = mutableListOf(Schule(-1, "Alle Schulen"))
        val schoolJson = response.optJSONArray("schulen")
        for (index in 0 until (schoolJson?.length() ?: 0)) {
            val school = schoolJson?.optJSONObject(index) ?: continue
            schools += Schule(school.optInt("id"), school.optString("name"))
        }

        spinner.setItems(schools)
        spinner.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<Any> {
            override fun onItemSelected(view: MaterialSpinner, position: Int, id: Long, item: Any) {
                val selectedSchool = item as? Schule ?: return
                val filtered = if (selectedSchool.id > 0) {
                    allChildren.filter { it.schoolId == selectedSchool.id }
                } else {
                    allChildren
                }
                bindChildren(filtered.ifEmpty { listOf(emptyPlaceholder()) })
            }
        })

        bindChildren(allChildren)
    }

    private fun bindChildren(items: List<ChildListItem>) {
        visibleChildren.clear()
        visibleChildren.addAll(items)
        recyclerView.adapter = ChildListAdapter(visibleChildren, this)
    }

    private fun emptyPlaceholder(): ChildListItem = ChildListItem(
        name = getString(R.string.noCheckin),
        school = "",
        grade = 0,
        checkedIn = false,
        detailUrl = "",
        schoolId = -1,
        hasBirthday = false,
        checkinUrl = "",
    )

    private fun setChildCount(number: Int) {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        titleView.text = "$currentDate ($number)"
    }

    interface OnItemSelectedListener

    companion object {
        const val TITLE = "Eingecheckt"
        fun newInstance() = FragmentChildListCheckin()
    }
}
