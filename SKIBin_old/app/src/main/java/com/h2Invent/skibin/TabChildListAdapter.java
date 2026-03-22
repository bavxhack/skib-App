package com.h2Invent.skibin;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabChildListAdapter extends FragmentStatePagerAdapter {
    private static int TAB_COUNT = 2;

    public TabChildListAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return FragmentChildListCheckin.newInstance();
            case 1:
                return FragmentChildListAfterList.newInstance();

        }
        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return FragmentChildListCheckin.TITLE;

            case 1:
                return FragmentChildListAfterList.TITLE;

        }
        return super.getPageTitle(position);
    }
}
