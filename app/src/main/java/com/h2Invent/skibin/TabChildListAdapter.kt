package com.h2Invent.skibin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabChildListAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val todayFragment = FragmentToday.newInstance()
    val checkinFragment = FragmentChildListCheckin.newInstance()
    val afterListFragment = FragmentChildListAfterList.newInstance()

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> todayFragment
        1 -> afterListFragment
        else -> checkinFragment
    }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> FragmentToday.TITLE
        1 -> "Kinder"
        else -> "Anwesend"
    }
}
