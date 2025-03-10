package com.example.baobook;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
//spinner adapter that sets colors, emojis, and text for each mood option
public class MoodSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> moodOptions;
    private int[] moodColors;
    private String[] moodEmojis;

    public MoodSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> moodOptions, int[] moodColors, String[] moodEmojis) {
        super(context, resource, moodOptions);
        this.context = context;
        this.moodOptions = moodOptions;
        this.moodColors = moodColors;
        this.moodEmojis = moodEmojis;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText(MoodUtils.getMoodEmoji(moodOptions.get(position)) + " " + moodOptions.get(position));
        view.setBackgroundColor(MoodUtils.getMoodColor(moodOptions.get(position)));
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(MoodUtils.getMoodEmoji(moodOptions.get(position)) + " " + moodOptions.get(position));
        view.setBackgroundColor(MoodUtils.getMoodColor(moodOptions.get(position)));
        return view;
    }
}