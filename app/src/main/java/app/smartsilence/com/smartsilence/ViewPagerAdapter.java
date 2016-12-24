package app.smartsilence.com.smartsilence;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import app.smartsilence.com.smartsilence.tabs_fragments.TabFragmentOne;
import app.smartsilence.com.smartsilence.tabs_fragments.TabFragmentTwo;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public ViewPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                TabFragmentOne tab1 = new TabFragmentOne();
                return tab1;
            case 1:
                TabFragmentTwo tab2 = new TabFragmentTwo();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

