package net.dividedattention.crowdvision.eventlist;

import net.dividedattention.crowdvision.BasePresenter;
import net.dividedattention.crowdvision.BaseView;
import net.dividedattention.crowdvision.data.CrowdEvent;

import java.util.List;

/**
 * Created by drewmahrt on 5/19/17.
 */

public interface EventListContract {
    interface View extends BaseView<Presenter>{
        void showNearbyEvent(CrowdEvent event);
        void showRemoteEvent(CrowdEvent event);
        void showExpiredEvent(CrowdEvent event);
        void showUpdatedNearbyEvent(int position);
        void showUpdatedRemoteEvent(int position);
        void showUpdatedExpiredEvent(int position);
    }

    interface Presenter extends BasePresenter{
        void loadEvents(String city, String state);
        void cleanUp();
        List<CrowdEvent> getNearbyEvents();
        List<CrowdEvent> getRemoteEvents();
        List<CrowdEvent> getExpiredEvents();
    }
}
