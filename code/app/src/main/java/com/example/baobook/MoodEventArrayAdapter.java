package com.example.baobook;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.baobook.model.MoodEvent;
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

        MoodEvent moodEvent = getItem(position);

        TextView moodText = view.findViewById(R.id.mood_state);
        TextView dateText = view.findViewById(R.id.mood_date);
        TextView timeText = view.findViewById(R.id.mood_time);
        TextView descriptionText = view.findViewById(R.id.mood_description);
        TextView social = view.findViewById(R.id.social_situation);
        View rootLayout = view.findViewById(R.id.mood_item_root);

        if (moodEvent != null) {
            // Format date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Set mood state with emoji and color using MoodUtils
            String moodString = moodEvent.getMood().toString();
            moodText.setText(MoodUtils.getMoodEmoji(moodString) + " " + moodString); // Add emoji
            moodText.setTextColor(MoodUtils.getMoodColor(moodString)); // Set color

            // Set date, time, and description
            dateText.setText("Date: " + dateFormat.format(moodEvent.getDate()));
            timeText.setText("Time: " + timeFormat.format(moodEvent.getTime()));
            descriptionText.setText("Trigger: " + moodEvent.getDescription());
            social.setText("Social: " + moodEvent.getSocial());


            GradientDrawable drawable = (GradientDrawable) rootLayout.getBackground();
            if (drawable != null) {
                drawable.setStroke(2, MoodUtils.getMoodColor(moodString)); // Set border color
            }
        }

        return view;
    }
}