package com.example.obinna.journalapp.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.obinna.journalapp.dao.EntryDao;
import com.example.obinna.journalapp.model.JournalEntry;


@Database(entities = {JournalEntry.class}, version = 1, exportSchema = false)
public abstract class EntryRoomDatabase extends RoomDatabase {

    public abstract EntryDao entryDao();

    private static EntryRoomDatabase INSTANCE;

    private static final Object LOCK = new Object();

    private static final String DATABASE_NAME = "journal_database";


    public static EntryRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        EntryRoomDatabase.class, DATABASE_NAME).build();
            }
        }
         return INSTANCE;
    }
}
