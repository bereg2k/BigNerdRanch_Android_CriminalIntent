package com.bignerdranch.android.criminalintent.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bignerdranch.android.criminalintent.R;

public class CrimePhotoViewerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_NAME = "fileName";
    private static final String EXTRA_TRANSITION_NAME = "transitionName";

    private View mPhotoContainer;
    private ImageView mPhotoPreview;

    public static Intent newIntent(Context context, String fileName, String transitionName) {
        Intent intent = new Intent(context, CrimePhotoViewerActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_TRANSITION_NAME, transitionName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo);

        mPhotoPreview = findViewById(R.id.photo_preview_image_view);
        mPhotoPreview.setImageBitmap(BitmapFactory.decodeFile(getIntent().getStringExtra(EXTRA_FILE_NAME)));
        mPhotoPreview.setTransitionName(getIntent().getStringExtra(EXTRA_TRANSITION_NAME));

        mPhotoContainer = findViewById(R.id.photo_preview_container);
        mPhotoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onBackPressed();
                return false;
            }
        });
    }
}
