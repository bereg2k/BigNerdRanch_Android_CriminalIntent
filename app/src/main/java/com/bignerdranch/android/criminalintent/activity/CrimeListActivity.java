package com.bignerdranch.android.criminalintent.activity;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.criminalintent.model.Crime;
import com.bignerdranch.android.criminalintent.fragment.CrimeFragment;
import com.bignerdranch.android.criminalintent.fragment.CrimeListFragment;
import com.bignerdranch.android.criminalintent.R;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks,
        CrimeFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected int getFragmentContainerResId() {
        return super.getFragmentContainerResId();
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeDeleted() {

        // code only for tablets
        if (findViewById(R.id.detail_fragment_container) != null) {
            CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);

            if (crimeFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(crimeFragment)
                        .commit();
            }
        }

        // updating the activity's state after removing any crime's details fragment on the screen
        recreate();
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);

        // only removing specific fragment with crime's details if current crime being deleted from the list
        if (crimeFragment != null && crimeFragment.getCrime().equals(crime)) {
            getSupportFragmentManager().beginTransaction()
                    .remove(crimeFragment)
                    .commit();
        }

        onCrimeUpdated(crime);
    }
}
