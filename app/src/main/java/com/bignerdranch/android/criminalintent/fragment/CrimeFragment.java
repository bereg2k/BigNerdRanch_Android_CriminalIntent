package com.bignerdranch.android.criminalintent.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bignerdranch.android.criminalintent.R;
import com.bignerdranch.android.criminalintent.activity.CrimePhotoViewerActivity;
import com.bignerdranch.android.criminalintent.activity.DatePickerActivity;
import com.bignerdranch.android.criminalintent.model.Crime;
import com.bignerdranch.android.criminalintent.model.CrimeLab;
import com.bignerdranch.android.criminalintent.util.PictureUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static android.widget.CompoundButton.OnCheckedChangeListener;
import static android.widget.CompoundButton.OnClickListener;
import static android.widget.CompoundButton.OnLongClickListener;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_PHOTO = "DialogPhoto";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHONE = 3;
    private static final int REQUEST_PHOTO = 4;

    private static final int PERMISSION_READ_CONTACTS_CODE = 100;

    private static final boolean IS_DATE_PICKER_DIALOG = true;

    private static final long POST_DELAY = 1000;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private TextView mDeleteTextView;

    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mCallSuspectButton;

    private ImageView mPhotoView;
    private Point mPhotoViewSize;
    private ImageButton mPhotoButton;

    private Callbacks mCallbacks;

    // private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm", Locale.getDefault());
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();

        // updating current crime object's record in DB (after pausing the fragment/its activity)
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mDateButton = view.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTablet(Objects.requireNonNull(getContext()))) {
                    FragmentManager manager = getFragmentManager();
                    DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DATE);
                } else {
                    startActivityForResult(DatePickerActivity.newIntent(getActivity(), mCrime.getDate()), REQUEST_DATE);
                }
            }
        });

        mTimeButton = view.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);

                updateCrime();
            }
        });

        mDeleteTextView = view.findViewById(R.id.delete_text_view);
        mDeleteTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                deleteCrime();
                            }
                        })
                        .create()
                        .show();
            }
        });

        final Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = view.findViewById(R.id.choose_suspect);
        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT);
            }
        });

        mSuspectButton.setOnLongClickListener(new OnLongClickListener() { // clearing out the suspect on Long Click
            @Override
            public boolean onLongClick(View v) {
                if (mCrime.getSuspect() == null) {
                    return false;
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.clear_suspect_dialog)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCrime.setSuspect(null);
                                updateSuspect();
                                mCallSuspectButton.setVisibility(View.GONE);
                            }
                        })
                        .create()
                        .show();

                return true;
            }
        });

        updateSuspect();

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mCallSuspectButton = view.findViewById(R.id.call_suspect);
        mCallSuspectButton.setVisibility(mCrime.getSuspect() != null ? View.VISIBLE : View.GONE);
        mCallSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // asking user to grant the READ_CONTACTS ("dangerous") permission, if it wasn't granted previously
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS_CODE);
                } else {
                    callSuspect();
                }
            }
        });

        mReportButton = view.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setChooserTitle(R.string.send_report)
                        .setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .createChooserIntent();

                startActivity(intent);
            }
        });


        mPhotoView = view.findViewById(R.id.crime_photo);
        mPhotoView.setTransitionName("image_details_" + getArguments().getSerializable(ARG_CRIME_ID).toString());
        mPhotoView.setOnClickListener(photoView -> {
            if (mPhotoFile == null || !mPhotoFile.exists()) {
                return;
            }
            Intent intent = CrimePhotoViewerActivity.newIntent(getActivity(), mPhotoFile.getPath(), photoView.getTransitionName());
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(), photoView, photoView.getTransitionName());
            startActivity(intent, options.toBundle());
        });

        mPhotoButton = view.findViewById(R.id.crime_camera);
        final Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mPhotoButton.setEnabled(mPhotoFile != null && photoCaptureIntent.resolveActivity(packageManager) != null);
        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(), "com.bignerdranch.android.criminalintent.fileprovider", mPhotoFile);
                photoCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity().getPackageManager()
                        .queryIntentActivities(photoCaptureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName, uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(photoCaptureIntent, REQUEST_PHOTO);
            }
        });

        mPhotoView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // getting actual size of the mPhotoView on the screen after layout pass
                        // this needs to be done for an effective photo scaling
                        mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mPhotoViewSize = new Point(mPhotoView.getWidth(), mPhotoView.getHeight());
                        updatePhotoView();
                    }
                });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri uriContact = data.getData();
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = getActivity().getContentResolver().query(uriContact, queryFields, null, null, null);

            try {
                if (cursor.getCount() == 0) {
                    return;
                }
                cursor.moveToFirst();
                String suspect = cursor.getString(0);

                mCrime.setSuspect(suspect);
                updateSuspect();
                mCallSuspectButton.setVisibility(View.VISIBLE);
            } finally {
                cursor.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();

            mPhotoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPhotoView.announceForAccessibility(getString(R.string.crime_photo_taken_description));
                }
            }, POST_DELAY);
        }

        updateCrime();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) { // processing after granting/revoking permission to some functions
            case PERMISSION_READ_CONTACTS_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callSuspect();
                } else {
                    mCallSuspectButton.setEnabled(false);
                }
        }
    }

    /**
     * Getting active crime from CrimeFragment.
     *
     * @return {@link Crime} object that is actively displayed on the CrimeFragment
     */
    public Crime getCrime() {
        return mCrime;
    }

    /**
     * Update Date button on the fragment with an actual available value.
     */
    private void updateDate() {
        mDateButton.setText(getString(R.string.date_picker_title) + " " + mSimpleDateFormat.format(mCrime.getDate()));
    }

    /**
     * Update Date button on the fragment with an actual available value.
     */
    private void updateTime() {
        mTimeButton.setText(getString(R.string.time_picker_title) + " " + mSimpleTimeFormat.format(mCrime.getDate()));
    }

    /**
     * Updating the state of the CHOOSE SUSPECT button
     */
    private void updateSuspect() {
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(getString(R.string.crime_suspect_current_text, mCrime.getSuspect()));
        } else {
            mSuspectButton.setText(R.string.crime_suspect_text);
        }
    }

    /**
     * Determine if the currently active device is Tablet or Phone
     *
     * @param context current application context
     * @return true if the device the app is running on is Tablet
     */
    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Constructing crime report text.
     *
     * @return string representation of the crime report text
     */
    private String getCrimeReport() {
        String solvedString = null;

        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEEE, MMM dd, HH:mm";
        String dateString = (String) DateFormat.format(dateFormat, mCrime.getDate());

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    /**
     * Calling the phone app to dial the number of the suspect from the Contacts.
     * If there's no phone number on the contact - alert will be shown.
     */
    private void callSuspect() {
        Cursor cursorID = getActivity().getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI,
                        new String[]{ContactsContract.Contacts._ID},
                        ContactsContract.Contacts.DISPLAY_NAME + " = ?",
                        new String[]{mCrime.getSuspect()},
                        null);
        String id;
        try {
            cursorID.moveToFirst();
            id = cursorID.getString(0);
        } finally {
            cursorID.close();
        }

        Cursor cursorPhoneNumber = getActivity().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null);

        String phoneNumber;

        if (cursorPhoneNumber.getCount() == 0) { // show alert if there's no phone number for the chosen contact
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error_dialog)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setMessage(R.string.no_phone_contact_dialog)
                    .create()
                    .show();

            return;
        }

        try {
            cursorPhoneNumber.moveToFirst();
            // columnIndex = 1 for ContactsContract.CommonDataKinds.Phone.NUMBER column in previous query
            phoneNumber = cursorPhoneNumber.getString(1);
        } finally {
            cursorPhoneNumber.close();
        }

        Uri uriNumber = Uri.parse("tel:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_DIAL, uriNumber);
        startActivityForResult(intent, REQUEST_PHONE);
    }

    /**
     * Updating photo in the PhotoView section of the fragment.
     */
    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        } else {
            Bitmap bitmap = mPhotoViewSize != null ?
                    PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoViewSize.x, mPhotoViewSize.y) :
                    PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
            //saveSmallerBitmapToFile(bitmap);
        }
    }

    /**
     * Updating crime for two-pane master-detail (tablet) layouts
     */
    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    /**
     * Deleting single crime for two-pane master-detail (tablet) layouts
     */
    private void deleteCrime() {
        CrimeLab.get(getActivity()).removeCrime(mCrime);
        CrimeLab.get(getActivity()).setCrimeDeleted(mCrime);
        mCallbacks.onCrimeDeleted(mCrime);
    }

    /**
     * Additional method to test scaled images on the layout.
     * Method saves {@link Bitmap} object to device's memory for a research.
     *
     * @param bitmap scaled bitmap on the layout
     */
    private void saveSmallerBitmapToFile(Bitmap bitmap) {
        String filePath = mCrime.getPhotoFilename().replace(".jpg", "_small.jpg");
        File file = new File(CrimeLab.get(getActivity()).getFilesDir(), filePath);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException eInt) {
                    eInt.printStackTrace();
                }
            }
        }
    }

    /**
     * Required interface for hosting activities.
     * Using that interface hosting activity can interact with its fragments.
     */
    public interface Callbacks {
        /**
         * Method for data updating on the indevidual crimes on the list while user makes changes
         * to their details.
         *
         * @param crime a single {@link Crime} object that represents a single list entity
         */
        void onCrimeUpdated(Crime crime);

        /**
         * Method for deleting an active/selected crime.
         *
         * @param crime a single {@link Crime} object that's active
         */
        void onCrimeDeleted(Crime crime);
    }
}