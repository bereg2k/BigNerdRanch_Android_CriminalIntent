package com.bignerdranch.android.criminalintent.fragment;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.criminalintent.R;

/**
 * This fragment was supposed to host a detailed view of the thumbnail photos.
 * Unfortunately, it was impossible to add a smooth animation/transition effect for a zoom action.
 * Hence, everything is set up through Activity:
 * {@link com.bignerdranch.android.criminalintent.activity.CrimePhotoViewerActivity}
 */
public class CrimePhotoViewerFragment extends Fragment {

    private static final String ARG_FILE = "file";
    private static final String ARG_TRANSITION_NAME = "transitionName";

    private View mPhotoContainer;
    private ImageView mPhotoPreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_photo, container, false);

        mPhotoPreview = view.findViewById(R.id.photo_preview_image_view);
        mPhotoPreview.setImageBitmap(BitmapFactory.decodeFile(getArguments().getString(ARG_FILE)));
        mPhotoPreview.setTransitionName(getArguments().getString(ARG_TRANSITION_NAME));

        mPhotoContainer = view.findViewById(R.id.photo_preview_container);
        mPhotoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getFragmentManager().popBackStack();
                return true;
            }
        });

        return view;
    }

    public static CrimePhotoViewerFragment newInstance(String filePath, String transitionName) {
        Bundle args = new Bundle();
        args.putString(ARG_FILE, filePath);
        args.putString(ARG_TRANSITION_NAME, transitionName);

        CrimePhotoViewerFragment fragment = new CrimePhotoViewerFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
