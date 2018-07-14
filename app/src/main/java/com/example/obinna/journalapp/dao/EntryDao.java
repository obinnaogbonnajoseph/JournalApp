package com.example.obinna.journalapp.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.obinna.journalapp.model.JournalEntry;

import java.util.List;

@Dao
public interface EntryDao {

    @Query("SELECT * FROM journal_table")
    LiveData<List<JournalEntry>> getAllEntries();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(JournalEntry prescriptions);

    @Delete
    int deleteMeds(JournalEntry... prescriptions);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateMeds(JournalEntry prescription);

}
