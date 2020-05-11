package com.bignerdranch.android.criminalintent.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.criminalintent.R;
import com.bignerdranch.android.criminalintent.activity.CrimePagerActivity;
import com.bignerdranch.android.criminalintent.model.Crime;
import com.bignerdranch.android.criminalintent.model.CrimeLab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;

public class CrimeListFragment extends Fragment {
    private static final int REQUEST_GENERATE_NUMBER = 0;

    private static final String SUBTITLE_VISIBLE_KEY = "subtitle_visible";
    private static final String DIALOG_GENERATE = "DialogGenerate";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int mFragmentPosition;
    private boolean mSubtitleVisible;

    private TextView mNoCrimesTextView;
    private Button mAddNewCrimeButton;
    private Button mGenerateCrimesButton;

    private Callbacks mCallbacks;
    private CrimeFragment.Callbacks mCrimeFragmentCallbacks;

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SUBTITLE_VISIBLE_KEY);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SUBTITLE_VISIBLE_KEY, mSubtitleVisible);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (CrimeListFragment.Callbacks) context;
        mCrimeFragmentCallbacks = (CrimeFragment.Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mCrimeFragmentCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }

        MenuItem deleteAllItem = menu.findItem(R.id.delete_all);
        if (CrimeLab.get(getActivity()).getCrimes().size() != 0) {
            deleteAllItem.setVisible(true);
        } else {
            deleteAllItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime: {
                addNewCrime();
                return true;
            }
            case R.id.show_subtitle: {
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            }
            case R.id.delete_all: {
                getActivity().invalidateOptionsMenu();
                deleteAllCrimes();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNoCrimesTextView = view.findViewById(R.id.no_crimes_text_view);

        mAddNewCrimeButton = view.findViewById(R.id.add_new_crime_button);
        mAddNewCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCrime();
            }
        });

        mGenerateCrimesButton = view.findViewById(R.id.generate_crimes_button);
        mGenerateCrimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                GenerateCrimesFragment dialog = new GenerateCrimesFragment();
                //dialog.setTargetFragment(CrimeListFragment.this, REQUEST_GENERATE_NUMBER);
                dialog.show(manager, DIALOG_GENERATE);
            }
        });

        updateUI();

        setUpSwipeToDismiss();

        return view;
    }

    /**
     * Update UI for crime list elements.
     */
    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            // to update "delete all items" in the toolbar menu (after resuming activity)
            getActivity().invalidateOptionsMenu();

            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
        updateNoCrimesDialog();
    }

    /**
     * Holder class to present a RecyclerView item list for regular crimes.
     */
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private ImageView mSolvedImageView;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        /**
         * Method for adapter to bind a crime data to Holder of the regular crime.
         *
         * @param crime crime instance from the adapter
         */
        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mSimpleDateFormat.format(mCrime.getDate()));
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
            mSolvedImageView.setContentDescription(
                    mSolvedImageView.getVisibility() == View.VISIBLE ?
                            getString(R.string.crime_solved_description)
                            : null
            );
        }

        @Override
        public void onClick(View view) {
            mFragmentPosition = getAdapterPosition();
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    /**
     * Holder class to present a RecyclerView item list for special crimes that require contacting police.
     * This ViewHolder displays different type of list item with "CONTACT POLICE" button on it and a red text font.
     */
    private class CrimePoliceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Button mPoliceButton;
        private Crime mCrime;
        private ImageView mSolvedImageView;

        public CrimePoliceHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime_police, parent, false));

            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mPoliceButton = itemView.findViewById(R.id.crime_police_button);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);

            mPoliceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToast(getString(R.string.police_called_dialog, mCrime.getTitle()));
                }
            });
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mSimpleDateFormat.format(mCrime.getDate()));
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
            mSolvedImageView.setContentDescription(
                    mSolvedImageView.getVisibility() == View.VISIBLE ?
                            getString(R.string.crime_solved_description)
                            : null
            );
            mPoliceButton.setContentDescription(getString(R.string.contact_police_description, mCrime.getTitle()));
        }


        @Override
        public void onClick(View view) {
            mFragmentPosition = getAdapterPosition();
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter {
        private List<Crime> mCrimes;

        /**
         * Constructor for adapter
         *
         * @param crimes list of crimes from the caller (fragment with a list of crimes)
         */
        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        /**
         * Creating different type of ViewHolder depending on the viewType parameter
         */
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            switch (viewType) {
                case 1:
                    return new CrimePoliceHolder(layoutInflater, parent);
                case 0:
                default:
                    return new CrimeHolder(layoutInflater, parent);
            }
        }


        /**
         * Displaying the data on the specified position.
         *
         * @param holder   holder instance to bind data to
         * @param position position of the ViewHolder to bind data to
         */
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            switch (getItemViewType(position)) {
                case 1:
                    ((CrimePoliceHolder) holder).bind(crime);
                    break;
                case 0:
                default:
                    ((CrimeHolder) holder).bind(crime);
            }
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            Crime crime = mCrimes.get(position);
            return crime.isRequiresPolice() ? 1 : 0;
        }

        /**
         * Setting crimes for adapter's purposes in case adapter already exists (for refreshing items list)
         *
         * @param crimes list of crimes
         */
        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }

    /**
     * Update state of the toolbar's subtitle
     */
    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = mSubtitleVisible ?
                getResources().getQuantityString(R.plurals.subtitle_format_plurals, crimeCount, crimeCount) :
                null;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subtitle);
    }

    /**
     * Update placeholder to show/hide placeholder for zero-crimes list.
     */
    private void updateNoCrimesDialog() {
        int crimeCount = CrimeLab.get(getActivity()).getCrimes().size();

        if (crimeCount == 0) {
            mNoCrimesTextView.setVisibility(View.VISIBLE);
            mAddNewCrimeButton.setVisibility(View.VISIBLE);
            mGenerateCrimesButton.setVisibility(View.VISIBLE);
        } else {
            mNoCrimesTextView.setVisibility(View.GONE);
            mAddNewCrimeButton.setVisibility(View.GONE);
            mGenerateCrimesButton.setVisibility(View.GONE);
        }
    }

    /**
     * Clearing out the list of the crimes, deleting all existing entities on the list.
     * Methods calls a simple {@link AlertDialog} to confirm user's choice.
     */
    private void deleteAllCrimes() {
        new AlertDialog.Builder(getActivity()) // show alert dialog to confirm deletion
                .setTitle(R.string.are_you_sure_dialog)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Crime crime : CrimeLab.get(getActivity()).getCrimes()) {
                            CrimeLab.get(getActivity()).removeCrime(crime);
                        }

                        mCallbacks.onCrimeDeleted();
                    }
                })
                .create()
                .show();
    }

    /**
     * Deleting a crime from the list.
     * Methods calls a simple {@link AlertDialog} to confirm user's choice.
     */
    private void deleteCrime(final Crime crime) {
        new AlertDialog.Builder(getActivity()) // show alert dialog to confirm deletion
                .setTitle(R.string.are_you_sure_dialog)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        mCrimeFragmentCallbacks.onCrimeUpdated(crime);
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CrimeLab.get(getActivity()).removeCrime(crime);

                        mCrimeFragmentCallbacks.onCrimeDeleted(crime);
                    }
                })
                .create()
                .show();
    }

    /**
     * Add new crime from CrimeListActivity.
     * User will be transported to details of the new crime (CrimePagerActivity).
     */
    private void addNewCrime() {
        Crime crime = new Crime();
        String crimeString = getContext().getString(R.string.new_crime);

        crime.setTitle(crimeString + " #" + (CrimeLab.get(getActivity()).getCrimes().size() + 1));
        crime.setDate(new Date(System.currentTimeMillis()));

        CrimeLab.get(getActivity()).addCrime(crime);
        updateUI();
        mCallbacks.onCrimeSelected(crime);
    }

    /**
     * Show short pop-up message in form of text toast
     *
     * @param message text for the toast
     */
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Required interface for hosting activities.
     * Using that interface hosting activity can interact with its fragments.
     */
    public interface Callbacks {
        /**
         * Method for selecting a single crime on the list.
         * <p>
         * Should either start a new {@link CrimePagerActivity} (for phones)
         * or display the details of the selected crime on the adjacent separate fragment (for tablets)
         * </p>
         *
         * @param crime a single {@link Crime} object that represents a single list entity
         */
        void onCrimeSelected(Crime crime);

        /**
         * Method for deleting all crimes from the list.
         */
        void onCrimeDeleted();
    }

    private void setUpSwipeToDismiss() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int adapterPosition = viewHolder.getAdapterPosition();
                Crime crimeToRemove = mAdapter.mCrimes.get(adapterPosition);
                deleteCrime(crimeToRemove);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }
}