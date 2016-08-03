package net.dividedattention.crowdvision;


import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by drewmahrt on 7/29/16.
 */
public class ExpandedPhotoFragment extends Fragment {
    private static final String IMAGE = "image";
    private int transitionName;

    public static ExpandedPhotoFragment newInstance(Bitmap bitmap, String transitionName) {
        Bundle args = new Bundle();
        args.putParcelable(IMAGE,bitmap);
        args.putString("transitionName",transitionName);
        ExpandedPhotoFragment fragment = new ExpandedPhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.expanded_photo_fragment,container,false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.findViewById(R.id.image).setTransitionName(getArguments().getString("transitionName"));
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap bitmap = getArguments().getParcelable(IMAGE);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
