package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {

    private static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    private Button mJumpFirstButton;
    private Button mJumpLastButton;
    private EditText mPageNumberEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mViewPager = findViewById(R.id.crime_view_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager, 1) {

            @NonNull
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        mJumpFirstButton = findViewById(R.id.crime_jump_first_button);
        mJumpLastButton = findViewById(R.id.crime_jump_last_button);
        mPageNumberEditText = findViewById(R.id.crime_edit_text_page);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i); // setting a current item via crimeId when opening from the list
                break;
            }
        }

        mJumpFirstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0); // jump to the first item on the list
            }
        });

        mJumpLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // jump to the last item on the list
                mViewPager.setCurrentItem(mCrimes.size() - 1);
            }
        });

        // Add a listener to watch for changing of pages
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                checkJumpButtons();
            }

            @Override
            public void onPageSelected(int position) {
                // intentionally left blank
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // intentionally left blank
            }
        });

        mPageNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(s == null || s.toString().equals(""))) {
                    // changing pages depending on the user input
                    mViewPager.setCurrentItem(Integer.valueOf(s.toString()) - 1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // intentionally left blank
            }
        });

        // when opening a crime from the list, need to check the state of "jump" buttons
        checkJumpButtons();
    }

    /**
     * Get an intent to start {@link CrimePagerActivity}.
     *
     * @param packageContext current context
     * @param crimeId        unique ID number for accessing all the crime's details from DB.
     * @return new {@link Intent} to start the activity
     */
    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    /**
     * Check the state of the "Jump to First/Last" buttons.
     * Method enables/disables the buttons depending on the index of the current page.
     * <p>
     * Also method updates "page #" edit box.
     */
    private void checkJumpButtons() {
        int currentPosition = mViewPager.getCurrentItem();

        if (currentPosition == 0 && mCrimes.size() == 1) {
            mJumpFirstButton.setEnabled(false);
            mJumpLastButton.setEnabled(false);
        } else if (currentPosition == 0) {
            mJumpFirstButton.setEnabled(false);
            mJumpLastButton.setEnabled(true);
        } else if (currentPosition == mCrimes.size() - 1) {
            mJumpFirstButton.setEnabled(true);
            mJumpLastButton.setEnabled(false);
        } else {
            mJumpFirstButton.setEnabled(true);
            mJumpLastButton.setEnabled(true);
        }

        // updating EditText with page numbers
        mPageNumberEditText.setText(String.valueOf(currentPosition + 1));
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        // empty implementation to preserve different-device architecture
        // all the updates happen when user gets back to the list of Crimes (and it gets an update from DB)
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        // with CrimePagerActivity (phone interface functionality) that's the only thing that's needed (beyond DB update)
        finish();
    }
}
