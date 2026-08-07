package com.h2Invent.skibin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ChildListAdapter(
    private val items: List<ChildListItem>,
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<ChildListAdapter.ChildListViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onCheckinClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.childlistelement, parent, false)
        return ChildListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildListViewHolder, position: Int) {
        val item = items[position]
        holder.textViewName.text = item.name
        holder.initials.text = item.name.split(' ').filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.take(1).uppercase(Locale.GERMAN) }
        holder.textViewSchool.text = item.school
        holder.textViewGrade.text = if (item.grade > 0) "${item.grade}. Klasse" else ""
        holder.hasBirthday.visibility = if (item.hasBirthday) View.VISIBLE else View.GONE
        holder.sickInfo.text = item.formatSickInfo(holder.itemView.context.getString(R.string.childListSickUntilLabel))
        holder.sickInfo.visibility = if (item.krank) View.VISIBLE else View.GONE
        holder.card.isClickable = item.schoolId != -1
        holder.card.isFocusable = item.schoolId != -1
        holder.itemView.isEnabled = item.schoolId != -1

        when {
            item.schoolId == -1 -> {
                holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
                holder.statusBadge.visibility = View.GONE
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.strokeSubtle)
                holder.checkinButton.visibility = View.GONE
                holder.chevron.visibility = View.GONE
                holder.checkinButton.isEnabled = false
            }
            item.krank -> {
                holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonDanger))
                holder.statusBadge.visibility = View.VISIBLE
                holder.statusBadge.text = holder.itemView.context.getString(R.string.childListStatusSick)
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonDanger))
                holder.statusBadge.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.surfaceMuted)
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.backgroundError)
                holder.checkinButton.visibility = View.GONE
                holder.checkinButton.isEnabled = false
                holder.chevron.visibility = View.VISIBLE
            }
            item.checkedIn -> {
                holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.backgroundSuccess))
                holder.statusBadge.visibility = View.VISIBLE
                holder.statusBadge.text = holder.itemView.context.getString(R.string.childListStatusCheckedIn)
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.phoneGreen))
                holder.statusBadge.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.surfaceMuted)
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.backgroundSuccess)
                holder.checkinButton.visibility = View.GONE
                holder.checkinButton.isEnabled = false
                holder.chevron.visibility = View.VISIBLE
            }
            else -> {
                holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.backgroundError))
                holder.statusBadge.visibility = View.VISIBLE
                holder.statusBadge.text = holder.itemView.context.getString(R.string.childListStatusOpen)
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.statusAmber))
                holder.statusBadge.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.warningSoft)
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.strokeSubtle)
                holder.checkinButton.visibility = View.VISIBLE
                holder.checkinButton.isEnabled = true
                holder.chevron.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ChildListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.childCard)
        val textViewName: TextView = itemView.findViewById(R.id.childElementName)
        val initials: TextView = itemView.findViewById(R.id.childInitials)
        val textViewSchool: TextView = itemView.findViewById(R.id.childElementSchule)
        val textViewGrade: TextView = itemView.findViewById(R.id.childElementKlasse)
        val sickInfo: TextView = itemView.findViewById(R.id.childSickInfo)
        val indicator: LinearLayout = itemView.findViewById(R.id.indicatorCheckin)
        val statusBadge: TextView = itemView.findViewById(R.id.childStatusBadge)
        val hasBirthday: TextView = itemView.findViewById(R.id.birthdayShow)
        val checkinButton: MaterialButton = itemView.findViewById(R.id.checkinButton)
        val chevron: TextView = itemView.findViewById(R.id.childChevron)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && items[position].schoolId != -1) {
                    listener.onItemClick(position)
                }
            }
            checkinButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onCheckinClick(position)
            }
        }
    }
}

private fun ChildListItem.formatSickInfo(untilLabel: String): String {
    val range = when {
        !krankVon.isNullOrBlank() && !krankBis.isNullOrBlank() -> "$krankVon - $krankBis"
        !krankBis.isNullOrBlank() -> "$untilLabel $krankBis"
        !krankVon.isNullOrBlank() -> krankVon
        else -> null
    }

    return listOfNotNull(range, krankBemerkung?.takeIf { it.isNotBlank() }).joinToString(" • ")
}
