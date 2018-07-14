package com.example.obinna.journalapp.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.example.obinna.journalapp.dao.EntryDao;
import com.example.obinna.journalapp.model.JournalEntry;
import com.example.obinna.journalapp.model.OnDBTaskComplete;

import java.util.List;

public class EntryRepository {

    private EntryDao mEntryDao;
    private LiveData<List<JournalEntry>> mEntries;
    public static long rowAdded;
    public static int rowDeleted, rowUpdated;

    public EntryRepository(Application application) {
        EntryRoomDatabase db = EntryRoomDatabase.getDatabase(application.getApplicationContext());
        mEntryDao = db.entryDao();
        mEntries = mEntryDao.getAllEntries();
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return mEntries;
    }

    public long insert(JournalEntry entry) {
        return mEntryDao.insert(entry);
    }

    public int delete(JournalEntry... entry) {
       return mEntryDao.deleteMeds(entry);
    }

    public int update(JournalEntry entry) {
        return mEntryDao.updateMeds(entry);
    }


    private static class insertAsyncTask extends AsyncTask<JournalEntry, Void, Long> implements
        OnDBTaskComplete{

        private EntryDao mAsyncTaskDao;
        long idAdded = 0;

        insertAsyncTask(EntryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Long doInBackground(JournalEntry... entries) {
            return mAsyncTaskDao.insert(entries[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            Log.d("EditActivity","Value of row inserted: " + aLong);
            rowAdded = aLong;
            idAdded = aLong;
        }

        @Override
        public int onRowsChanged() {
            return 0;
        }

        @Override
        public long onRowsAdded() {
            return idAdded;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<JournalEntry, Void, Integer> implements OnDBTaskComplete {

        private EntryDao mAsyncTaskDao;
        private Integer rowsAffected = 0;

        deleteAsyncTask(EntryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Integer doInBackground(JournalEntry... entries) {
            // Return the number of rows deleted
            return  mAsyncTaskDao.deleteMeds(entries);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            rowDeleted = integer;
            rowsAffected = integer;
        }

        @Override
        public int onRowsChanged() {
            return rowsAffected;
        }

        @Override
        public long onRowsAdded() {
            return 0;
        }
    }

    private static class updateAsyncTask extends AsyncTask<JournalEntry, Void, Integer> implements
        OnDBTaskComplete{

        private EntryDao mAsyncTaskDao;
        private int rowsUpdated = 0;

        updateAsyncTask(EntryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Integer doInBackground(JournalEntry... entries) {
            return mAsyncTaskDao.updateMeds(entries[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            rowUpdated = integer;
            rowsUpdated = integer;
        }

        @Override
        public int onRowsChanged() {
            return rowsUpdated;
        }

        @Override
        public long onRowsAdded() {
            return 0;
        }
    }
}
