package com.raytracer.uniwuemensa;

import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by sw-ch_000 on 29.05.2015.
 */
public class ScrollingViewPager extends ViewPager {
    public ScrollingViewPager(Context context) {
        super(context);
    }

    public ScrollingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isFirstElementVisible() {
        MainActivity.MealListFragment frag = (MainActivity.MealListFragment)((FragmentPagerAdapter)getAdapter()).getItem(getCurrentItem());
        return frag.isFirstElementVisible();
    }
}
