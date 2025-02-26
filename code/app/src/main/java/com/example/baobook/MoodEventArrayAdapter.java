package com.example.baobook;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {
    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moods) {
        super(context, 0, moods);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        Log.d("MoodEventArrayAdapter", "getView: position=" + position);

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content, parent, false);
        } else {
            view = convertView;
        }

        MoodEvent mood = getItem(position);

        TextView moodState = view.findViewById(R.id.mood_state);
        TextView moodDate = view.findViewById(R.id.mood_date);
        TextView moodTime = view.findViewById(R.id.mood_time);
        TextView moodDescription = view.findViewById(R.id.mood_description);

        if (mood != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            moodState.setText(mood.getState());
            moodDate.setText(dateFormat.format(mood.getDate())); // Format date
            moodTime.setText(timeFormat.format(mood.getTime())); // Format time
            moodDescription.setText(mood.getDescription());
        }

        return view;
    }
}
