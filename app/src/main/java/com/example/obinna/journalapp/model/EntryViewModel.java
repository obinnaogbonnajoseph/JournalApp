package com.example.obinna.journalapp.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.obinna.journalapp.data.EntryRepository;

import java.util.List;

public class EntryViewModel extends AndroidViewModel {

    private EntryRepository mRepository;

    private LiveData<List<JournalEntry>> mEntries;

    public EntryViewModel(@NonNull Application application) {
        super(application);
        mRepository = new EntryRepository(application);
        mEntries = mRepository.getAllEntries();
    }

    public LiveData<List<JournalEntry>> getAllEntries() { return mEntries; }

    public long insert(JournalEntry entry) { return mRepository.insert(entry); }

    public int delete(JournalEntry... entry) {return mRepository.delete(entry);}

    public int update(JournalEntry entry) {return mRepository.update(entry);}
}

