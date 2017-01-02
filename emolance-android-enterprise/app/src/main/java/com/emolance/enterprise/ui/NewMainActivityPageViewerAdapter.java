package com.emolance.enterprise.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by yusun on 6/22/15.
 */
public class NewMainActivityPageViewerAdapter extends FragmentStatePagerAdapter {

    public NewMainActivityPageViewerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return new AdminFragment();
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "New";
        } else {
            return "Historical";
        }
    }
}
