package com.example.obinna.journalapp.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.obinna.journalapp.dao.EntryDao;
import com.example.obinna.journalapp.model.JournalEntry;

import java.util.List;

public class EntryRepository {

    private EntryDao mEntryDao;
    private LiveData<List<JournalEntry>> mEntries;

    public EntryRepository(Application application) {
        EntryRoomDatabase db = EntryRoomDatabase.getDatabase(application.getApplicationContext());
        mEntryDao = db.entryDao();
        mEntries = mEntryDao.getAllEntries();
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return mEntries;
    }

    public void insert(JournalEntry entry) {
        new insertAsyncTask(mEntryDao).execute(entry);
    }

    public void delete(JournalEntry entry) {
        new deleteAsyncTask(mEntryDao).execute(entry);
    }

    public void update(JournalEntry entry) {
        new updateAsyncTask(mEntryDao).execute(entry);
    }

    private static class insertAsyncTask extends AsyncTask<JournalEntry, Void, Void> {

        private EntryDao mAsyncTaskDao;

        insertAsyncTask(EntryDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(JournalEntry... entries) {
            mAsyncTaskDao.insert(entries[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<JournalEntry, Void, Void> {

        private EntryDao mAsyncTaskDao;

        deleteAsyncTask(EntryDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(JournalEntry... entries) {
            mAsyncTaskDao.deleteMeds(entries[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<JournalEntry, Void, Void> {

        private EntryDao mAsyncTaskDao;

        updateAsyncTask(EntryDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(JournalEntry... entries) {
            mAsyncTaskDao.updateMeds(entries[0]);
            return null;
        }
    }
}
