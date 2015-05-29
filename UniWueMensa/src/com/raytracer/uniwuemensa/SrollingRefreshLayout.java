package com.raytracer.uniwuemensa;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.example.uniwuemensa.R;

import java.util.List;


public class SrollingRefreshLayout extends SwipeRefreshLayout {
    public SrollingRefreshLayout(Context context) {
        super(context);
    }

    public SrollingRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp()
    {
        ScrollingViewPager view = (ScrollingViewPager) findViewById(R.id.pager);

        return !view.isFirstElementVisible();
    }
}
