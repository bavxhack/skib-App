package com.h2Invent.skibin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabChildListAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val checkinFragment = FragmentChildListCheckin.newInstance()
    val afterListFragment = FragmentChildListAfterList.newInstance()

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> checkinFragment
        else -> afterListFragment
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> FragmentChildListCheckin.TITLE
        else -> FragmentChildListAfterList.TITLE
    }
}
