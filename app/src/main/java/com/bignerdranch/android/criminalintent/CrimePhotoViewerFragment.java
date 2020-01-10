package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CrimePhotoViewerFragment extends DialogFragment {

    private static final String ARG_FILE = "file";

    private ImageView mPhotoPreview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_photo, null, false);

        mPhotoPreview = view.findViewById(R.id.photo_preview_image_view);
        mPhotoPreview.setImageBitmap(BitmapFactory.decodeFile(getArguments().getString(ARG_FILE)));

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null, false);

        mPhotoPreview = view.findViewById(R.id.photo_preview_image_view);
        mPhotoPreview.setImageBitmap(BitmapFactory.decodeFile(getArguments().getString(ARG_FILE)));

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    public static CrimePhotoViewerFragment newInstance(String filePath) {
        Bundle args = new Bundle();
        args.putString(ARG_FILE, filePath);

        CrimePhotoViewerFragment fragment = new CrimePhotoViewerFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
