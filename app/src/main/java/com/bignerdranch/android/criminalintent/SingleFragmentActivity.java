package com.bignerdranch.android.criminalintent;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    /**
     * Create a main fragment for an activity to inflate into getFragmentContainerResId()
     *
     * @return new main single fragment for this activity.
     */
    protected abstract Fragment createFragment();

    /**
     * Method for dynamic layout binding.
     * In case any child classes need to provide various implementation depending on different factors
     * (like, type of device - phone or tablet).
     */
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @IdRes
    protected int getFragmentContainerResId() {
        return R.id.fragment_container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(getFragmentContainerResId());

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(getFragmentContainerResId(), fragment)
                    .commit();
        }
    }
}
