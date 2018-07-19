package com.example.obinna.journalapp.ui;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.obinna.journalapp.R;
import com.example.obinna.journalapp.adapter.JournalRecyclerViewAdapter;
import com.example.obinna.journalapp.model.EntryViewModel;
import com.example.obinna.journalapp.model.JournalEntry;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
GoogleApiClient.OnConnectionFailedListener, JournalRecyclerViewAdapter.AdapterOnClickHandler,
FirebaseAuth.AuthStateListener {

    public static final String ENTRY = "com.example.obinna.journalapp.ENTRY";
    // Set up the firebase and google variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    // Set up the views
    private TextView emptyView;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFAB;
    // Toolbar
    private ActionBar mActionBar;
    // View Model instance
    private EntryViewModel mModel;
    // list data to initialize database
    private List<JournalEntry> listEntries;
    // list data for deleting selected items
    private List<JournalEntry> mDeleteList = new ArrayList<>();
    // Recycler adapter
    private JournalRecyclerViewAdapter mAdapter;
    // Create an instance of delete async task
    deleteAsyncTask deleteTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.id.main_activity_title);
        // Set up the action bar
        mActionBar = getSupportActionBar();
        // Set up the views
        mRecyclerView = findViewById(R.id.rv_journal_list);
        mFAB = findViewById(R.id.main_fab);
        mProgressBar = findViewById(R.id.loading_indicator);
        emptyView = findViewById(R.id.error_view);
        // Get view model from ViewModelProvider
        mModel = ViewModelProviders.of(this).get(EntryViewModel.class);

        // Initialize the FirebaseAuth and set up google authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.addAuthStateListener(this);
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Instantiate the async task
        deleteTask = new deleteAsyncTask(mModel);


        // If user has not previously logged in, start the login activity
        if(mFirebaseUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            // Not signed in, launch the login activity
            startActivity(intent);
            finish();
            return;
        } else {        // User have logged in, sign in.
            // Get the user name
            String userName = mFirebaseUser.getDisplayName();
            if (userName != null) {
                int endIndex = userName.indexOf(" ");
                String userFirstName = userName.substring(0,endIndex);
                String title = "Hello, " + userFirstName;
                if(title.length() < 25) {
                    setTitle(title);
                } else setTitle("Hello, User");
            }
            // Keep reference in sync
            mDatabase.child("entries").child(mFirebaseUser.getUid()).keepSynced(true);
        }
        // start the google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Set up the adapter and recycler view
        mAdapter = new JournalRecyclerViewAdapter(this,this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));

        // add observer for LiveData returned by getAllEntries()
        mModel.getAllEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable final List<JournalEntry> journalEntries) {
                if(journalEntries != null) {
                    if(journalEntries.isEmpty()) {
                        // Populate from the firebase database
                        List<JournalEntry> journals = populateData();
                        if(journals != null && !journals.isEmpty()) {
                            Log.d("MainActivity","Data from fb size: " + journals.size());
                            // Show progress bar
                            mProgressBar.setVisibility(View.VISIBLE);
                            for(JournalEntry journal : journals) {
                                mModel.insert(journal);
                            }
                            // set up the adapter
                            mAdapter.setmEntries(journals);
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }

                        if(journals != null && journals.isEmpty()) {
                            emptyView.setText(R.string.text_empty_view);
                            mRecyclerView.setVisibility(View.INVISIBLE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }else {
                        mAdapter.setmEntries(journalEntries);
                        emptyView.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // Set up the FAB
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens the Edit Activity
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                } else startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final List<JournalEntry> entries = mModel.getAllEntries().getValue();
        switch(item.getItemId()) {
            case R.id.action_delete_all:
                if(entries!=null && !entries.isEmpty()) {
                    // Create an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Delete all memories?")
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // convert the list to an array
                                    JournalEntry[] deleteEntries = entries.toArray(new JournalEntry[entries.size()]);
                                    // Delete the entries
                                    deleteTask.execute(deleteEntries);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Cancel the dialog
                                    if(dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                            }).create().show();
                } else Toast.makeText(MainActivity.this,"Nothing to delete",
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_log_out:
                // Log out
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                // return to log in page
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else startActivity(intent);
                // Close and tidy up activity
                finish();
                return true;
            case R.id.action_delete_main:
                // delete the items
                // Create an alert dialog, to warn user of delete action
                // First check that list is not empty
                if(!mDeleteList.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Delete selected memories?")
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // convert the list to an array
                                    JournalEntry[] deleteSelected = mDeleteList.toArray(new JournalEntry[mDeleteList.size()]);
                                    // Delete the entries
                                    deleteTask.execute(deleteSelected);
                                    // clear the list
                                    mDeleteList.clear();
                                    // Make all selection to be false
                                    List<JournalEntry> allSelection = mModel.getAllEntries().getValue();
                                    if(allSelection != null && !allSelection.isEmpty()) {
                                        for (int i = 0; i < allSelection.size(); i++) {
                                            allSelection.get(i).selected = false;
                                        }
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    // reset the menu item
                                    invalidateOptionsMenu();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Cancel the dialog
                                    if(dialog != null) {
                                        dialog.dismiss();
                                        // clear the list
                                        mDeleteList.clear();
                                        // Make all selection to be false
                                        List<JournalEntry> allSelection = mModel.getAllEntries().getValue();
                                        if(allSelection != null && !allSelection.isEmpty()) {
                                            for (int i = 0; i < allSelection.size(); i++) {
                                                allSelection.get(i).selected=false;
                                            }
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    // reset the menu item
                                    invalidateOptionsMenu();
                                }
                            }).create().show();

                } else Toast.makeText(MainActivity.this, "Nothing selected to delete",Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home:
                // Clear the list
                mDeleteList.clear();
                // Make all selection to be false
                List<JournalEntry> allSelection = mModel.getAllEntries().getValue();
                if(allSelection != null && !allSelection.isEmpty()) {
                    for (int i = 0; i < allSelection.size(); i++) {
                        allSelection.get(i).selected=false;
                    }
                    mAdapter.notifyDataSetChanged();
                }
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteAllItem = menu.findItem(R.id.action_delete_all);
        MenuItem logOutItem = menu.findItem(R.id.action_log_out);
        MenuItem deleteSelectedItem = menu.findItem(R.id.action_delete_main);
        if(mDeleteList.isEmpty()) {
            deleteAllItem.setVisible(true);
            logOutItem.setVisible(true);
            deleteSelectedItem.setVisible(false);
            mFAB.setVisibility(View.VISIBLE);
            mActionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            deleteAllItem.setVisible(false);
            logOutItem.setVisible(false);
            deleteSelectedItem.setVisible(true);
            mFAB.setVisibility(View.INVISIBLE);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        // Verify that user is not null
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(null == currentUser) {
            // return to log in page
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            // Close and tidy up activity
            finish();
        }
    }

    /**
     * Takes care of possible memory leak of the async task
     * Also removes the firebase authentication listener
     */
    @Override
    protected void onDestroy() {
        // remove authentication state listener
        mFirebaseAuth.removeAuthStateListener(this);
        // cancels async task if it is still running when activity is destroyed
        if(deleteTask.getStatus() == AsyncTask.Status.RUNNING) {
            deleteTask.cancel(true);
            deleteTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(JournalEntry entry) {
        if(mDeleteList.isEmpty()) {
            Intent intent = new Intent(MainActivity.this,ViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(ENTRY,entry);
            intent.putExtra(ENTRY,bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            } else startActivity(intent);
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onLongClick(int position) {
        mDeleteList.add(mModel.getAllEntries().getValue().get(position));
        mAdapter.notifyItemChanged(position);
        // Set up the necessary views
        invalidateOptionsMenu();
    }

    @Override
    public void onDeleteEntry(JournalEntry entry) {
        // Delete entry from list.
        mDeleteList.remove(entry);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Google play services error.\n Please retry later.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(a);
    }

    /**
     * Takes care of possible memory leak of the async task
     */
    @Override
    protected void onStop() {
        if(deleteTask.getStatus() == AsyncTask.Status.RUNNING) {
            deleteTask.cancel(true);
            deleteTask = null;
        }
        super.onStop();
    }

    /**
     * Async task that takes care of the delete database operation.
     */
    @SuppressLint("StaticFieldLeak")
    private class deleteAsyncTask extends AsyncTask<JournalEntry, Void, Integer> {

        private EntryViewModel viewModel;

        deleteAsyncTask(EntryViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        protected Integer doInBackground(JournalEntry... journalEntries) {
            return viewModel.delete(journalEntries);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer > 0) {
                Toast.makeText(MainActivity.this,"Notes deleted.",Toast.LENGTH_SHORT).show();
            } else Toast.makeText(MainActivity.this,"Something went wrong.\n Notes not deleted.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private List<JournalEntry> populateData() {
        listEntries = new ArrayList<>();

        if (mFirebaseUser != null) {
            String userId = mFirebaseUser.getUid();
            mDatabase.child("entries").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                List<JournalEntry> entries = new ArrayList<>();

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot shot : dataSnapshot.getChildren()) {
                        entries.add(shot.getValue(JournalEntry.class));
                    }
                    listEntries.addAll(entries);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("MainActivity","Some error occurred in getting firebase db values");
                }
            });
            return listEntries;
        }else return null;
    }
}
