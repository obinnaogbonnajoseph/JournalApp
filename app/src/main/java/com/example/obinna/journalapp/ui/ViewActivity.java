package com.example.obinna.journalapp.ui;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.obinna.journalapp.R;
import com.example.obinna.journalapp.model.EntryViewModel;
import com.example.obinna.journalapp.model.JournalEntry;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Formatter;

import static com.example.obinna.journalapp.ui.MainActivity.ENTRY;

public class ViewActivity extends AppCompatActivity {

    // Field variables
    private JournalEntry mEntry;
    private EntryViewModel mViewModel;
    private DatabaseReference databaseReference;
    // Create an instance of delete async task
    deleteAsyncTask deleteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        // Get the actionbar and set back enabled
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Instantiate the view model
        mViewModel = ViewModelProviders.of(ViewActivity.this).get(EntryViewModel.class);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //String userId = mAuth.getUid();
        // Instantiate the delete async task
        // Instantiate the async task
        deleteTask = new deleteAsyncTask(mViewModel);

        // Handle intent from other activities
        handleIntent();

        // Get the views and assign its value
        TextView textView = findViewById(R.id.text_entry);
        TextView categoryView = findViewById(R.id.view_activity_category_view);
        TextView timeView = findViewById(R.id.view_activity_time_view);
        textView.setText(mEntry.getEntry());

        // Set the title and time view of the activity
        Formatter fmt = new Formatter();
        long currentTime = mEntry.getEntryTime();
        String activityTitle = fmt.format("%ta, %te %th",currentTime,currentTime,currentTime).toString();
        setTitle(activityTitle);
        fmt = new Formatter();
        String timeActivity = fmt.format("%tI:%tM %tp",currentTime,currentTime,currentTime).toString();
        timeView.setText(timeActivity);

        // Set the category
        switch(mEntry.getmEntryType()) {
            case 0:
                categoryView.setText(R.string.text_uncategorized);
                categoryView.setTextColor(ContextCompat.getColor(this,R.color.green));
                categoryView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_round_bg,0,0,0);
                break;
            case 1:
                categoryView.setText(R.string.text_personal);
                categoryView.setTextColor(ContextCompat.getColor(this,R.color.yellow));
                categoryView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.yellow_round_bg,0,0,0);
                break;
            case 2:
                categoryView.setText(R.string.text_work);
                categoryView.setTextColor(ContextCompat.getColor(this,R.color.blue));
                categoryView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue_round_bg,0,0,0);

                break;
        }

        // Set up the FAB
        FloatingActionButton floatingActionButton = findViewById(R.id.view_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send intent to the edit activity
                Intent intent = new Intent(ViewActivity.this,EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ENTRY,mEntry);
                intent.putExtra(ENTRY,bundle);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation(ViewActivity.this).toBundle());
                } else startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete:
                // Set up an alert dialog
                // Create an AlertDialog.Builder and set the message, and click listeners
                // for the positive and negative buttons on the dialog.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Delete this memory?")
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the data
                                deleteTask.execute(mEntry);
                                // Return to previous activity
                                onBackPressed();
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
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        // Get the journal entry
        Intent intent = getIntent();
        if(intent.hasExtra(ENTRY)) {
            mEntry = intent.getBundleExtra(ENTRY).getParcelable(ENTRY);
        }
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
     * Takes care of possible memory leak of the async task
     */
    @Override
    protected void onDestroy() {
        if(deleteTask.getStatus() == AsyncTask.Status.RUNNING) {
            deleteTask.cancel(true);
            deleteTask = null;
        }
        super.onDestroy();
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
                Toast.makeText(ViewActivity.this,"Note deleted.",Toast.LENGTH_SHORT).show();
            } else Toast.makeText(ViewActivity.this,"Something went wrong.\nNote not deleted.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
