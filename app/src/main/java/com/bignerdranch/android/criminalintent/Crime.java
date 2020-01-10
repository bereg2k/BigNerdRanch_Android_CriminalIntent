package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Class to store information about registered crimes.
 * Typically a single crime can consist of several features:
 * <ul>
 * <li>ID</li>
 * <li>Title</li>
 * <li>Date</li>
 * <li>Solved/Not Solved flag</li>
 * <li>Requires Police/Not Requires Police flag</li>
 * </ul>
 */
public class Crime {

    //number of days from the current date to generate the dates for crimes
    private static final int RANDOM_DATE_RANGE = 30;

    private UUID mId;
    private Date mDate;
    private String mTitle;
    private boolean mSolved;
    private String mSuspect;

    private boolean mRequiresPolice;

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = getRandomDate(RANDOM_DATE_RANGE);
        mRequiresPolice = (int) (Math.random() + 0.5) == 1;
    }

    public UUID getId() {
        return mId;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isRequiresPolice() {
        return mRequiresPolice;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public void setRequiresPolice(boolean requiresPolice) {
        mRequiresPolice = requiresPolice;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    /**
     * Get random date between now and certain number of days to the past
     *
     * @return number of days to randomly go back to
     */
    private static Date getRandomDate(int numOfDays) {
        return new Date((long) (System.currentTimeMillis() - Math.random() * 86_400_000L * numOfDays));
    }

    /**
     * Returns the name of the file for crime's photo
     *
     * @return string representation of the filename (e.g. "IMG_7924749344.jpg")
     */
    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, mTitle, mDate, mSolved);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o.getClass() == getClass())) {
            return false;
        }
        Crime object = (Crime) o;
        return object.mId.equals(mId) &&
                object.mTitle.equals(mTitle) &&
                object.mDate.equals(mDate) &&
                object.mSolved == mSolved &&
                object.mRequiresPolice == mRequiresPolice;
    }
}
