package net.dividedattention.crowdvision.eventlist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dividedattention.crowdvision.R;
import net.dividedattention.crowdvision.data.CrowdEvent;

import java.util.List;

/**
 * Created by drewmahrt on 11/9/16.
 */

public class EventListFragment extends Fragment {
    private static final String TAG = "EventListFragment";

    private RecyclerView mRecyclerView;
    private EventListRecyclerViewAdapter mAdapter;
    private List<CrowdEvent> mEventList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_list_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.events_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        mAdapter = new EventListRecyclerViewAdapter(mEventList);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void showAddedEvent(){
        if(mAdapter != null) {
            mAdapter.notifyItemInserted(mEventList.size() - 1);
        }
    }

    public void showUpdatedEvent(int position){
        if(mAdapter != null){
            mAdapter.notifyItemChanged(position);
        }
    }

    public void setEvents(List<CrowdEvent> events){
        mEventList = events;
        if(mAdapter != null){
            mAdapter.swapData(events);
        }
    }

    public static Fragment newInstance(Bundle bundle, List<CrowdEvent> events){
        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(bundle);
        fragment.setEvents(events);
        return fragment;
    }
}
