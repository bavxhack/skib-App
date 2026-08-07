package com.h2Invent.skibin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jaredrummler.materialspinner.MaterialSpinner
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentChildListAfterList : Fragment(), ChildListAdapter.OnItemClickListener {
    private var itemSelectedListener: OnItemSelectedListener? = null
    private val requestQueue by lazy { Volley.newRequestQueue(requireContext()) }

    private lateinit var titleView: TextView
    private lateinit var spinner: MaterialSpinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: TextInputEditText
    private lateinit var filterGroup: ChipGroup

    private var childListUrl: String = ""
    private var userToken: String = ""
    private val allChildren = mutableListOf<ChildListItem>()
    private val visibleChildren = mutableListOf<ChildListItem>()
    private var selectedSchoolId = -1
    private var statusFilter = StatusFilter.ALL
    private val activeCheckins = mutableSetOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        itemSelectedListener = context as? OnItemSelectedListener
            ?: error("Parent activity must implement OnItemSelectedListener")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_kinderliste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView = view.findViewById(R.id.url)
        spinner = view.findViewById(R.id.schulenChooseListe)
        recyclerView = view.findViewById(R.id.recyclerKinderListe)
        searchView = view.findViewById(R.id.childSearch)
        filterGroup = view.findViewById(R.id.childStatusFilters)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(value: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(value: CharSequence?, start: Int, before: Int, count: Int) = applyFilters()
            override fun afterTextChanged(value: Editable?) = Unit
        })
        filterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            statusFilter = when (checkedIds.firstOrNull()) {
                R.id.filterWaiting -> StatusFilter.WAITING
                R.id.filterPresent -> StatusFilter.PRESENT
                R.id.filterSick -> StatusFilter.SICK
                else -> StatusFilter.ALL
            }
            applyFilters()
        }
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
                .putExtra(ChildDetailActivity.EXTRA_CHECKIN_URL, selectedChild.checkinUrl)
                .putExtra(ChildListMainActivity.EXTRA_USER_TOKEN, userToken),
        )
    }

    override fun onCheckinClick(position: Int) {
        val selectedChild = visibleChildren.getOrNull(position) ?: return
        val checkinUrl = selectedChild.checkinUrl
        if (checkinUrl.isBlank()) return

        AlertDialog.Builder(requireContext())
            .setTitle(selectedChild.name)
            .setMessage(R.string.checkinTitle)
            .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int ->
                executeCheckin(checkinUrl)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun executeCheckin(checkinUrl: String) {
        if (!activeCheckins.add(checkinUrl)) return
        val uri = "$checkinUrl?communicationToken=$userToken"
        val request = JsonObjectRequest(Request.Method.GET, uri, null,
            { response ->
                activeCheckins.remove(checkinUrl)
                Toast.makeText(context, response.optString("errorText"), Toast.LENGTH_LONG).show()
                (activity as? ChildListMainActivity)?.initApp()
            },
            { error ->
                activeCheckins.remove(checkinUrl)
                Toast.makeText(context, error.localizedMessage ?: "Request failed", Toast.LENGTH_LONG).show()
            },
        )
        requestQueue.add(request)
    }

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
                selectedSchoolId = selectedSchool.id
                applyFilters()
            }
        })

        val result = response.optJSONArray("result")
        for (index in 0 until (result?.length() ?: 0)) {
            val child = result?.optJSONObject(index) ?: continue
            allChildren += child.toChildListItem()
        }

        val number = if (response.has("number") && !response.isNull("number")) response.optInt("number") else allChildren.size
        setChildCount(number)

        updateFilterCounts()
        applyFilters()
    }

    private fun applyFilters() {
        if (!::recyclerView.isInitialized) return
        val query = if (::searchView.isInitialized) searchView.text?.toString().orEmpty().trim() else ""
        val filtered = allChildren.filter { child ->
            val matchesSchool = selectedSchoolId <= 0 || child.schoolId == selectedSchoolId
            val matchesName = query.isBlank() || child.name.contains(query, ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.WAITING -> !child.checkedIn && !child.krank
                StatusFilter.PRESENT -> child.checkedIn
                StatusFilter.SICK -> child.krank
            }
            matchesSchool && matchesName && matchesStatus
        }
        bindChildren(filtered.ifEmpty { listOf(emptyPlaceholder()) })
    }

    private fun updateFilterCounts() {
        view?.findViewById<com.google.android.material.chip.Chip>(R.id.filterAll)?.text = "Alle · ${allChildren.size}"
        view?.findViewById<com.google.android.material.chip.Chip>(R.id.filterWaiting)?.text = "Nicht da · ${allChildren.count { !it.checkedIn && !it.krank }}"
        view?.findViewById<com.google.android.material.chip.Chip>(R.id.filterPresent)?.text = "Anwesend · ${allChildren.count { it.checkedIn }}"
        view?.findViewById<com.google.android.material.chip.Chip>(R.id.filterSick)?.text = "Krank · ${allChildren.count { it.krank }}"
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
        krank = false,
        krankVon = null,
        krankBis = null,
        krankBemerkung = null,
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
        const val TITLE = "Anwesend nach Liste"
        fun newInstance() = FragmentChildListAfterList()
    }

    private enum class StatusFilter { ALL, WAITING, PRESENT, SICK }
}
