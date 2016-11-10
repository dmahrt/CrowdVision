package net.dividedattention.crowdvision.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import net.dividedattention.crowdvision.fragments.EventListFragment;
import net.dividedattention.crowdvision.models.CrowdEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drewmahrt on 11/9/16.
 */

public class EventListPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<CrowdEvent> mCurrentEvents, mRemoteEvents, mExpiredEvents;

    public EventListPagerAdapter(FragmentManager fm, ArrayList<CrowdEvent> currentEvents,
                                 ArrayList<CrowdEvent> remoteEvents, ArrayList<CrowdEvent> expiredEvents) {
        super(fm);
        mCurrentEvents = currentEvents;
        mRemoteEvents = remoteEvents;
        mExpiredEvents = expiredEvents;
    }

    @Override
    public int getCount() {
        return 3;
    }


    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        switch (position){
            case 0:
                return EventListFragment.newInstance(bundle,mCurrentEvents);
            case 1:
                return EventListFragment.newInstance(bundle,mRemoteEvents);
            default:
                return EventListFragment.newInstance(bundle,mExpiredEvents);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Nearby";
            case 1:
                return "Remote";
            default:
                return "Expired";
        }
    }
}
