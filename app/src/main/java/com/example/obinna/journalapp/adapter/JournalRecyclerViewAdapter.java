package com.example.obinna.journalapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.obinna.journalapp.R;
import com.example.obinna.journalapp.model.JournalEntry;

import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;


public class JournalRecyclerViewAdapter extends RecyclerView.Adapter<
        JournalRecyclerViewAdapter.EntryViewHolder> {

    private final LayoutInflater mInflater;

    private AdapterOnClickHandler mClickHandler;

    private Context context;

    private List<JournalEntry> mEntries; // Cached copy of entries

    public interface AdapterOnClickHandler {
        void onClick(JournalEntry entry);
    }

    public JournalRecyclerViewAdapter(Context context, AdapterOnClickHandler clickHandler) {
        mInflater = LayoutInflater.from(context);
        mClickHandler = clickHandler;
        this.context = context;
    }


    @NonNull
    @Override
    public JournalRecyclerViewAdapter.EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                        int viewType) {
        return new EntryViewHolder(mInflater.inflate(R.layout.recyclerview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerViewAdapter.EntryViewHolder holder, int position) {
        if(mEntries != null) {
            // Sort the entries here:
            Collections.sort(mEntries,new SortByDate());
            // Get the current data.
            JournalEntry entry = mEntries.get(position);
            // Set the summary text
            holder.summary.setText(entry.getEntry());
            // Map the category to a specific integer and set category text
            switch(entry.getmEntryType()) {
                case 0:
                    holder.category.setText(R.string.text_uncategorized);
                    holder.category.setTextColor(ContextCompat.getColor(context,R.color.green));
                    break;
                case 1:
                    holder.category.setText(R.string.text_personal);
                    holder.category.setTextColor(ContextCompat.getColor(context,R.color.yellow));
                    break;
                case 2:
                    holder.category.setText(R.string.text_work);
                    holder.category.setTextColor(ContextCompat.getColor(context,R.color.blue));
                    break;
            }
            // Set the date text
            Formatter fmt = new Formatter();
            long currentTime = entry.getEntryTime();
            String dateInfo = fmt.format("%td/%tm %tR",currentTime,currentTime,currentTime).toString();
            holder.dateView.setText(dateInfo);

            // Format the date and set the text
        } else {
            // Change the values accordingly
            holder.summary.setText(R.string.text_null_value);
            holder.category.setText(R.string.text_null_value);
            holder.dateView.setText(R.string.text_null_value);
        }
    }

    public void setmEntries(List<JournalEntry> entries) {
        mEntries = entries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(mEntries != null) return mEntries.size();
        else return 0;
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private final TextView summary, category, dateView;

        EntryViewHolder(View itemView) {
            super(itemView);
            summary = itemView.findViewById(R.id.summary_view);
            category = itemView.findViewById(R.id.category_view);
            dateView = itemView.findViewById(R.id.date_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(mEntries.get(getAdapterPosition()));
        }
    }

    // implement a sorting class
    class SortByDate implements Comparator<JournalEntry> {

        @Override
        public int compare(JournalEntry entry1, JournalEntry entry2) {
            int value1 = (int) entry1.getEntryTime();
            int value2 = (int) entry2.getEntryTime();

            return value1 > value2 ? -1 : (value1 < value2) ? 1 : 0;
        }
    }
}

