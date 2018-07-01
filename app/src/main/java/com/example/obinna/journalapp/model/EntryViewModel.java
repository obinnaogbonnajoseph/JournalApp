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

    public void insert(JournalEntry entry) {mRepository.insert(entry); }

    public void delete(JournalEntry entry) {mRepository.delete(entry);}

    public void update(JournalEntry entry) {mRepository.update(entry);}

}

