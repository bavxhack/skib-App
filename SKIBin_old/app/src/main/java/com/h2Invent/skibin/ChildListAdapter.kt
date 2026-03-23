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
        holder.textViewSchool.text = item.school
        holder.textViewGrade.text = if (item.grade > 0) "${item.grade}. Klasse" else ""
        holder.hasBirthday.visibility = if (item.hasBirthday) View.VISIBLE else View.GONE

        when {
            item.schoolId == -1 -> {
                holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
                holder.statusBadge.visibility = View.GONE
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.strokeSubtle)
                holder.checkinButton.visibility = View.GONE
                holder.checkinButton.isEnabled = false
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
            }
            else -> {
                holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.backgroundError))
                holder.statusBadge.visibility = View.VISIBLE
                holder.statusBadge.text = holder.itemView.context.getString(R.string.childListStatusOpen)
                holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary))
                holder.statusBadge.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.surfaceMuted)
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.strokeSubtle)
                holder.checkinButton.visibility = View.VISIBLE
                holder.checkinButton.isEnabled = true
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ChildListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.childCard)
        val textViewName: TextView = itemView.findViewById(R.id.childElementName)
        val textViewSchool: TextView = itemView.findViewById(R.id.childElementSchule)
        val textViewGrade: TextView = itemView.findViewById(R.id.childElementKlasse)
        val indicator: LinearLayout = itemView.findViewById(R.id.indicatorCheckin)
        val statusBadge: TextView = itemView.findViewById(R.id.childStatusBadge)
        val hasBirthday: TextView = itemView.findViewById(R.id.birthdayShow)
        val checkinButton: MaterialButton = itemView.findViewById(R.id.checkinButton)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onItemClick(position)
            }
            checkinButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onCheckinClick(position)
            }
        }
    }
}
