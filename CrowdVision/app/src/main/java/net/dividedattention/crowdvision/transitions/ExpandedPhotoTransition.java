package net.dividedattention.crowdvision.transitions;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.AttributeSet;

/**
 * Created by drewmahrt on 7/29/16.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ExpandedPhotoTransition extends TransitionSet {
    public ExpandedPhotoTransition() {
        init();
    }

    /**
     * This constructor allows us to use this transition in XML
     */
    public ExpandedPhotoTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}