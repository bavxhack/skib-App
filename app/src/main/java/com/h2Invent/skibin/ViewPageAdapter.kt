package com.h2Invent.skibin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ViewPageAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val connectionFragment = FragmentConnection.newInstance()
    private val settingsFragment = FragmentSettings.newInstance()

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> connectionFragment
        else -> settingsFragment
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> FragmentConnection.TITLE
        else -> FragmentSettings.TITLE
    }
}
