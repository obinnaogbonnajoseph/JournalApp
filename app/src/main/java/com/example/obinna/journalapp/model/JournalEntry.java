package com.example.obinna.journalapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


@Entity(tableName = "journal_table")
public class JournalEntry implements Parcelable{

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private long mId;

    @ColumnInfo(name = "journal_entry")
    private String mEntry;

    @ColumnInfo(name = "entry_type")
    public int mEntryType;

    @ColumnInfo(name = "entry_time")
    private long mEntryTime;

    @Ignore
    public boolean selected = false;

    @Ignore
    public JournalEntry() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public JournalEntry(long mId, @NonNull String entry, int entryType, long entryTime) {
        this.mId = mId;
        this.mEntry = entry;
        this.mEntryType = entryType;
        this.mEntryTime = entryTime;
    }


    protected JournalEntry(Parcel in) {
        mId = in.readLong();
        mEntry = in.readString();
        mEntryType = in.readInt();
        mEntryTime = in.readLong();
    }

    public static final Creator<JournalEntry> CREATOR = new Creator<JournalEntry>() {
        @Override
        public JournalEntry createFromParcel(Parcel in) {
            return new JournalEntry(in);
        }

        @Override
        public JournalEntry[] newArray(int size) {
            return new JournalEntry[size];
        }
    };

    // Set getter and setter methods
    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getEntry() {
        return mEntry;
    }

    public void setmEntry(@NonNull String mEntry) {
        this.mEntry = mEntry;
    }

    public int getmEntryType() {
        return mEntryType;
    }

    public void setmEntryType(int mEntryType) {
        this.mEntryType = mEntryType;
    }

    public long getEntryTime() {
        return mEntryTime;
    }

    public void setmEntryTime(long mEntryTime) {
        this.mEntryTime = mEntryTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mEntry);
        dest.writeInt(mEntryType);
        dest.writeLong(mEntryTime);
    }
}
