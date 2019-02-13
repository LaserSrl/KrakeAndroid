package com.krake.core.widget;

import android.util.SparseArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by joel on 29/10/15.
 */
public abstract class CachedFragmentPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> mFragments;

    public CachedFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new SparseArray<>();
    }

    @Override
    final public Fragment getItem(int position) {
        Integer positionKey = Integer.valueOf(position);
        Fragment fragment = mFragments.get(positionKey);
        if (fragment == null) {
            fragment = createFragment(position);
            mFragments.put(positionKey, fragment);
        }
        return fragment;
    }

    protected abstract Fragment createFragment(int position);

}
