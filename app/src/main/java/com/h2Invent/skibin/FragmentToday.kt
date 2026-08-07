package com.h2Invent.skibin

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The user's operational landing page, backed only by the two existing child-list APIs. */
class FragmentToday : Fragment() {
    private val requestQueue by lazy { Volley.newRequestQueue(requireContext()) }
    private var checkinUrl = ""
    private var childrenUrl = ""
    private var userToken = ""
    private var presentChildren = emptyList<ChildListItem>()
    private var openChildren = emptyList<ChildListItem>()
    private var completedRequests = 0

    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var presentMetric: TextView
    private lateinit var openCount: TextView
    private lateinit var sickCount: TextView
    private lateinit var progress: LinearProgressIndicator
    private lateinit var previewContainer: LinearLayout
    private lateinit var emptyView: TextView
    private lateinit var syncView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_today, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        refresh = view.findViewById(R.id.todayRefresh)
        presentMetric = view.findViewById(R.id.todayPresentMetric)
        openCount = view.findViewById(R.id.todayOpenCount)
        sickCount = view.findViewById(R.id.todaySickCount)
        progress = view.findViewById(R.id.todayProgress)
        previewContainer = view.findViewById(R.id.todayPreviewContainer)
        emptyView = view.findViewById(R.id.todayEmpty)
        syncView = view.findViewById(R.id.todaySync)

        val preferences = requireContext().getSharedPreferences(SaveSettings.SHARED_PREFS, 0)
        val userName = preferences.getString(SaveSettings.USER_NAME, "").orEmpty()
            .trim().substringBefore(' ').ifBlank { getString(R.string.todayDefaultName) }
        view.findViewById<TextView>(R.id.todayGreeting).text = getString(R.string.todayGreeting, userName)
        view.findViewById<TextView>(R.id.todayOrganisation).text =
            preferences.getString(SaveSettings.USER_ORG, getString(R.string.todayAllSchools))
        view.findViewById<TextView>(R.id.todayDate).text =
            SimpleDateFormat("EEEE, d. MMMM", Locale.GERMAN).format(Date())
                .replaceFirstChar { it.titlecase(Locale.GERMAN) }
        view.findViewById<TextView>(R.id.todayShowAll).setOnClickListener {
            (activity as? ChildListMainActivity)?.showPage(1)
        }
        refresh.setColorSchemeResources(R.color.colorPrimary)
        refresh.setOnRefreshListener(::loadDashboard)
        render()
        if (checkinUrl.isNotBlank() && childrenUrl.isNotBlank()) loadDashboard()
    }

    fun setConfig(checkinUrl: String, childrenUrl: String, token: String) {
        this.checkinUrl = checkinUrl
        this.childrenUrl = childrenUrl
        userToken = token
        if (view != null && checkinUrl.isNotBlank() && childrenUrl.isNotBlank() && token.isNotBlank()) {
            loadDashboard()
        }
    }

    private fun loadDashboard() {
        if (userToken.isBlank()) return
        completedRequests = 0
        refresh.isRefreshing = true
        requestChildren(checkinUrl) { presentChildren = it }
        requestChildren(childrenUrl) { openChildren = it }
    }

    private fun requestChildren(url: String, update: (List<ChildListItem>) -> Unit) {
        if (url.isBlank()) {
            requestFinished()
            return
        }
        requestQueue.add(JsonObjectRequest(Request.Method.GET, "$url?communicationToken=$userToken", null,
            { response ->
                update(parseChildren(response))
                requestFinished()
            },
            { requestFinished() },
        ))
    }

    private fun parseChildren(response: JSONObject): List<ChildListItem> {
        if (response.optBoolean("error")) return emptyList()
        val result = response.optJSONArray("result") ?: return emptyList()
        return buildList {
            for (index in 0 until result.length()) result.optJSONObject(index)?.let { add(it.toChildListItem()) }
        }
    }

    private fun requestFinished() {
        completedRequests++
        if (completedRequests >= 2) {
            refresh.isRefreshing = false
            syncView.text = getString(R.string.todaySyncedNow)
            render()
        }
    }

    private fun render() {
        val present = presentChildren.size
        val open = openChildren.count { !it.krank }
        val sick = (presentChildren + openChildren).distinctBy { it.name }.count { it.krank }
        val total = present + open
        presentMetric.text = getString(R.string.todayMetric, present, total)
        openCount.text = open.toString()
        sickCount.text = sick.toString()
        progress.progress = if (total == 0) 0 else (present * 100 / total)

        previewContainer.removeAllViews()
        val upcoming = openChildren.filterNot { it.krank }.take(2)
        emptyView.visibility = if (upcoming.isEmpty()) View.VISIBLE else View.GONE
        upcoming.forEach { previewContainer.addView(createPreviewCard(it)) }
    }

    private fun createPreviewCard(child: ChildListItem): View {
        val context = requireContext()
        val card = MaterialCardView(context).apply {
            radius = dp(18).toFloat()
            cardElevation = 0f
            strokeWidth = dp(1)
            strokeColor = ContextCompat.getColor(context, R.color.strokeSubtle)
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.surfaceCard))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) }
            isClickable = true
            isFocusable = true
            setOnClickListener { openDetail(child) }
        }
        val row = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(14), dp(13), dp(12), dp(13))
        }
        val avatar = TextView(context).apply {
            text = initials(child.name)
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            setTypeface(typeface, Typeface.BOLD)
            background = ContextCompat.getDrawable(context, R.drawable.bg_avatar)
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        }
        val copy = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) }
            addView(TextView(context).apply {
                text = child.name + if (child.hasBirthday) "  🎈" else ""
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(typeface, Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = listOf("Klasse ${child.grade}", child.school).filter { it.isNotBlank() }.joinToString(" · ")
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.grey))
                setPadding(0, dp(3), 0, 0)
            })
        }
        val button = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle).apply {
            text = getString(R.string.checkinButton)
            minWidth = 0
            insetTop = 0
            insetBottom = 0
            layoutParams = LinearLayout.LayoutParams(-2, dp(44))
            setOnClickListener { openDetail(child) }
        }
        row.addView(avatar)
        row.addView(copy)
        row.addView(button)
        card.addView(row)
        return card
    }

    private fun openDetail(child: ChildListItem) {
        if (child.detailUrl.isBlank()) return
        startActivity(Intent(requireContext(), ChildDetailActivity::class.java)
            .putExtra(ChildDetailActivity.EXTRA_URL, child.detailUrl)
            .putExtra(ChildListMainActivity.EXTRA_USER_TOKEN, userToken))
    }

    private fun initials(name: String): String = name.split(' ').filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.take(1).uppercase(Locale.GERMAN) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val TITLE = "Heute"
        fun newInstance() = FragmentToday()
    }
}
