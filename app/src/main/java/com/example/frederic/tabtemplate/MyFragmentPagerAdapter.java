package com.example.frederic.tabtemplate;

// import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by frederic on 9/17/15.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] {"Basic", "Map"};
    private Context context;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return PAGE_COUNT;
    }

    @Override
    public  Fragment getItem(int pos)
    {
        switch (pos) {
            case 0:
                return MapFragment.newInstance();
            case 1:
                return TabFragment.newInstance(context);
            default:
                return MapFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int pos)
    {
        return tabTitles[pos];
    }

}
