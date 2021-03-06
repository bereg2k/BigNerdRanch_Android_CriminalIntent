package com.bignerdranch.android.criminalintent.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.criminalintent.fragment.DatePickerFragment;

import java.util.Date;

public class DatePickerActivity extends SingleFragmentActivity {

    private static final String EXTRA_CRIME_DATE = "com.bignerdranch.android.criminalintent.crime_date";

    @Override
    protected Fragment createFragment() {
        return DatePickerFragment.newInstance((Date) getIntent().getSerializableExtra(EXTRA_CRIME_DATE));
    }

    public static Intent newIntent(Context context, Date date) {
        Intent intent = new Intent(context, DatePickerActivity.class);
        intent.putExtra(EXTRA_CRIME_DATE, date);
        return intent;
    }
}
