package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class GenerateCrimesFragment extends DialogFragment {
    private static final int DEFAULT_CRIMES_GENERATE = 1;
    private static final int MAX_CRIME_NUMBER = 10;

    private static final String CRIME_NUMBER_KEY = "crime_number";

    private EditText mCrimeNumberEditText;
    private ImageButton mMoreCrimesButton;
    private ImageButton mLessCrimesButton;
    private int mCrimeNumber = DEFAULT_CRIMES_GENERATE;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(CRIME_NUMBER_KEY, mCrimeNumber);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCrimeNumber = savedInstanceState.getInt(CRIME_NUMBER_KEY);
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_generate_crimes, null);

        mCrimeNumberEditText = view.findViewById(R.id.generate_crimes_number_edit_text);
        mCrimeNumberEditText.setText(String.valueOf(mCrimeNumber));
        mCrimeNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(s == null || s.toString().equals(""))) {
                    updateNavButtons();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mLessCrimesButton = view.findViewById(R.id.less_crimes_button);
        mLessCrimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCrimeNumber--;
                mCrimeNumberEditText.setText(String.valueOf(mCrimeNumber));
            }
        });

        mMoreCrimesButton = view.findViewById(R.id.more_crimes_button);
        mMoreCrimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCrimeNumber++;
                mCrimeNumberEditText.setText(String.valueOf(mCrimeNumber));
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.how_many_crimes_generate_dialog)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CrimeLab.get(getActivity()).generateNewCrimes(mCrimeNumber);
                        dialog.dismiss();
                        getActivity().recreate();
                    }
                })
                .create();
    }

    /**
     * Updating the state of navigation buttons (increasing/decreasing number of crimes to generate)
     */
    private void updateNavButtons() {
        if (mCrimeNumber == 0) {
            mLessCrimesButton.setEnabled(false);
            mLessCrimesButton.setColorFilter(Color.argb(255, 128, 128, 128));
            mMoreCrimesButton.setEnabled(true);
        } else if (mCrimeNumber == MAX_CRIME_NUMBER) {
            mLessCrimesButton.setEnabled(true);
            mMoreCrimesButton.setEnabled(false);
            mMoreCrimesButton.setColorFilter(Color.argb(255, 128, 128, 128));
        } else {
            mLessCrimesButton.setEnabled(true);
            mLessCrimesButton.clearColorFilter();
            mMoreCrimesButton.setEnabled(true);
            mMoreCrimesButton.clearColorFilter();
        }
    }
}
