package com.bignerdranch.android.criminalintent.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.android.criminalintent.R;
import com.bignerdranch.android.criminalintent.util.db.CrimeBaseHelper;
import com.bignerdranch.android.criminalintent.util.db.CrimeCursorWrapper;
import com.bignerdranch.android.criminalintent.util.db.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for storing collection of {@link Crime} objects.
 * <p>Singleton-pattern class.</p>
 */
public class CrimeLab {
    private static final boolean IS_NEW_CRIMES_RANDOM_TITLES = false;

    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private Crime mCrimeDeleted;

    /**
     * Public getter for CrimeLab instance (either create one if it doesn't exist, or provide the existing one
     *
     * @param context current context
     * @return CrimeLab instance
     */
    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    /**
     * Private constructor to keep a single instance of the class in the program.
     *
     * @param context current context
     */
    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    /**
     * Extracting crimes from DB table into the List-collection of {@link Crime} objects.
     *
     * @return List of Crime objects
     */
    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        // sql query to get ALL crimes from crimes tables (no filters)
        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    /**
     * Extracting a single crime from DB table into single {@link Crime} object.
     *
     * @param id ID of the crime
     * @return single Crime object
     */
    public Crime getCrime(UUID id) {
        // sql query with a filter by ID
        CrimeCursorWrapper cursor = queryCrimes(CrimeTable.Cols.UUID + " = ?",
                new String[]{id.toString()});

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    /**
     * Getting a crime that was recently deleted
     *
     * @return recently deleted crime object
     */
    public Crime getCrimeDeleted() {
        return mCrimeDeleted;
    }

    /**
     * Setting a crime that was recently deleted.
     * Clients MUST set null after processing a delete event
     * to indicate that there're no recently deleted crimes anymore.
     *
     * @param crimeDeleted recently deleted crime object
     */
    public void setCrimeDeleted(Crime crimeDeleted) {
        mCrimeDeleted = crimeDeleted;
    }

    /**
     * Getting file path on the device to a certain crime
     *
     * @param crime certain {@link Crime} object
     * @return file path to photo of the given crime
     */
    public File getPhotoFile(Crime crime) {
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, crime.getPhotoFilename());
    }

    public File getFilesDir() {
        return mContext.getFilesDir();
    }

    /**
     * Add crime in DB table.
     *
     * @param crime Crime instance
     */
    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);

        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    /**
     * Update crime in DB table after changes
     *
     * @param crime Crime instance
     */
    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    /**
     * Removing crime from DB table.
     *
     * @param crime Crime instance.
     */
    public void removeCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    /**
     * Converting {@link Crime} object to {@link ContentValues} object.
     *
     * @param crime Crime object for data extraction
     * @return ContentValues object from extracted data
     */
    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();

        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        values.put(CrimeTable.Cols.POLICE, crime.isRequiresPolice() ? 1 : 0);

        return values;
    }

    /**
     * Method for obtaining convenient {@link CrimeCursorWrapper} - a wrapper class for {@link Cursor}
     * from querying the crimes DB table.
     * <p>Basically, method performs SELECT statement on the {@link CrimeTable#NAME} with necessary filters.
     *
     * @param whereClause WHERE section of the query with a column name to filter
     * @param whereArgs   values for filtering column
     * @return Cursor object to result of the query in the {@link CrimeCursorWrapper} form
     */
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);

        return new CrimeCursorWrapper(cursor);
    }

    /**
     * Public method to make CrimeLab to generate new list of {@link Crime} objects
     *
     * @param number number of crimes to create
     */
    public void generateNewCrimes(int number) {
        generateCrimesInternal(number);
    }

    /**
     * Generate number of crimes for CrimeLab
     *
     * @param number number of random generated crimes
     */
    private void generateCrimesInternal(int number) {
        String crimeString = mContext.getString(R.string.crime);

        for (int i = 0; i < number; i++) {
            Crime crime = new Crime();
            crime.setTitle(crimeString + " #" + (i + 1));
            crime.setSolved((int) (Math.random() + 0.5) == 1);

            crime.setRequiresPolice((int) (Math.random() + 0.5) == 1);

            ContentValues values = getContentValues(crime);
            mDatabase.insert(CrimeTable.NAME, null, values);
        }
    }
}
