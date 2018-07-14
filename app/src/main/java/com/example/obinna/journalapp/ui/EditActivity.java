package com.example.obinna.journalapp.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.obinna.journalapp.R;
import com.example.obinna.journalapp.model.EntryViewModel;
import com.example.obinna.journalapp.model.JournalEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.obinna.journalapp.ui.MainActivity.ENTRY;

public class EditActivity extends AppCompatActivity {

    private Spinner mSpinner;
    private int mEntryType = 0;
    private JournalEntry mEntry;
    private EditText mEditText;
    private DatabaseReference mDatabase;
    private String mUser;
    // Create a view model instance
    private EntryViewModel mViewModel;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        // Adding Toolbar to Main screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Edit");
        // Set up the views and necessary field values
        mViewModel = ViewModelProviders.of(this).get(EntryViewModel.class);
        mSpinner = findViewById(R.id.edit_activity_spinner);
        mEditText = findViewById(R.id.edit_text_edit_activity);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getUid();

        // Set up the spinner
        setUpSpinner();
        // Handle the intent
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_activity,menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_done:
                // Save the items to database
                long currentTime = Calendar.getInstance().getTime().getTime();
                String inputText = mEditText.getText().toString();

                // Ensure that the input text is never empty
                if(TextUtils.isEmpty(inputText)) {
                    Toast.makeText(EditActivity.this,"Empty text not allowed",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                // Case of update: mEntry is not null
                if(mEntry != null) {
                    // Update the data
                    mEntry.setmEntry(inputText);
                    mEntry.setmEntryType(mEntryType);
                    mEntry.setmEntryTime(currentTime);
                    // Get the rows updated. Run this in an asyncTask
                    new updateAsyncTask(mViewModel).execute(mEntry);
                    // Return to previous activity
                    onBackPressed();
                    return true;
                } else {        // Case of new entry
                    // Create a new data entry
                    JournalEntry entry = new JournalEntry(currentTime,inputText,mEntryType, currentTime);
                    new insertAsyncTask(mViewModel).execute(entry);
                    // Return to previous activity
                    onBackPressed();
                    return true;
                }
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Discard your changes?")
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Discard changes
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleIntent(Intent intent) {
        if(intent.hasExtra(ENTRY)) {
            // load up the data from the intent
            Bundle bundle = intent.getBundleExtra(ENTRY);
            mEntry = bundle.getParcelable(ENTRY);
            if (mEntry != null) {
                mEditText.setText(mEntry.getEntry());
                mEditText.setSelection(mEntry.getEntry().length());
                mSpinner.setSelection(mEntry.getmEntryType());
            }
        } else {
            mEntry = null;
            mSpinner.setSelection(0);
        }
    }

    /**
     * Method to set up the spinner
     */
    private void setUpSpinner() {
        // Create adapter for spinner.
        List<SpinnerList> list = new ArrayList<>();
        list.add(new SpinnerList(getResources().getString(R.string.text_uncategorized),R.drawable.green_round_bg,R.color.green));
        list.add(new SpinnerList(getResources().getString(R.string.text_personal),R.drawable.yellow_round_bg,R.color.yellow));
        list.add(new SpinnerList(getResources().getString(R.string.text_work),R.drawable.blue_round_bg,R.color.blue));

        CustomAdapter adapter = new CustomAdapter(EditActivity.this,list);
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerList selection = (SpinnerList) parent.getItemAtPosition(position);
                String stringSelection = selection.getTextName();
                if (!TextUtils.isEmpty(stringSelection)) {
                    if (stringSelection.equals(getString(R.string.text_uncategorized))) {
                        // Set up the entry type
                        mEntryType = 0;
                    } else if (stringSelection.equals(getString(R.string.text_personal))) {
                        // Set up the entry type
                        mEntryType = 1;
                    } else if (stringSelection.equals(getString(R.string.text_work))) {
                        // Set up the entry type
                        mEntryType =2;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Set entry type to be equal to zero
                mEntryType = 0;
            }
        });
    }

    /**
     * Custom adapter class for the spinner items
     */
    class CustomAdapter extends ArrayAdapter<SpinnerList> {

       List<SpinnerList> list;

        private Context mContext;

        CustomAdapter(@NonNull Context context, List<SpinnerList> list) {
            super(context, 0, list);
            mContext = context;
            this.list = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.spinner_item_layout,parent,
                    false);

            SpinnerList currentItem = list.get(position);

            TextView view = convertView.findViewById(R.id.text1);
            view.setText(currentItem.getTextName());
            view.setCompoundDrawablesWithIntrinsicBounds(currentItem.getDrawableResource(),0,
                    0,0);
            view.setTextColor(ContextCompat.getColor(mContext,currentItem.getTextColor()));

            return convertView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(mContext).inflate(R.layout.spinner_item_layout,parent,
                        false);

            SpinnerList currentItem = list.get(position);

            TextView view = convertView.findViewById(R.id.text1);
            view.setText(currentItem.getTextName());
            view.setCompoundDrawablesWithIntrinsicBounds(currentItem.getDrawableResource(),0,
                    0,0);
            view.setTextColor(ContextCompat.getColor(mContext,currentItem.getTextColor()));

            return convertView;
        }


    }

    /**
     * Class for the spinner list items
     */
    class SpinnerList {
        private String textName;
        private int drawableResource;
        private int textColor;

        SpinnerList(String textName, int drawableResource, int textColor) {
            this.textName = textName;
            this.drawableResource = drawableResource;
            this.textColor = textColor;
        }

        private String getTextName() {return textName;}

        private int getDrawableResource() {return drawableResource;}

        private int getTextColor() {return textColor;}
    }

    class updateAsyncTask extends AsyncTask<JournalEntry,Void,Integer> {

        private EntryViewModel model;

        updateAsyncTask(EntryViewModel model) {
            this.model = model;
        }

        @Override
        protected Integer doInBackground(JournalEntry... journalEntries) {
            return model.update(journalEntries[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer > 0) {
                Toast.makeText(EditActivity.this,"Note updated successfully",Toast.LENGTH_SHORT)
                        .show();
            } else Toast.makeText(EditActivity.this,"Something went wrong.\n Note not updated",Toast.LENGTH_SHORT)
                    .show();
        }
    }

    class insertAsyncTask extends AsyncTask<JournalEntry,Void,Long> {

        private EntryViewModel model;

        insertAsyncTask(EntryViewModel model) {
            this.model = model;
        }

        @Override
        protected Long doInBackground(JournalEntry... journalEntries) {
            return model.insert(journalEntries[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if(aLong > 0) {
                Toast.makeText(EditActivity.this,"Note added successfully",Toast.LENGTH_SHORT)
                        .show();
            } else Toast.makeText(EditActivity.this,"Something went wrong.\n Note not added",Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
