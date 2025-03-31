package com.example.baobook;

/*
This fragment pops up when you tap on a mood authored by another user.
It simply displays the full details of the mood event.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.baobook.model.MoodEvent;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MoodEventDetailsFragment extends DialogFragment {
    private static final String TAG = "MoodDetailsFragment";

    private final MoodEvent moodEvent;

    public MoodEventDetailsFragment(MoodEvent clickedMood) {
        this.moodEvent = clickedMood;
        Log.d(TAG, "MoodDetailsFragment created with mood: " + clickedMood.getMood());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Creating dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        Dialog dialog = null;

        try {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View view = inflater.inflate(R.layout.mood_event_details, null);
            Log.d(TAG, "Dialog layout inflated");

            ImageView moodImage = view.findViewById(R.id.mood_image);
            TextView moodState = view.findViewById(R.id.mood_state);
            TextView moodSocial = view.findViewById(R.id.mood_social);
            TextView moodTrigger = view.findViewById(R.id.mood_trigger);
            TextView moodDate = view.findViewById(R.id.mood_date);
            TextView moodTime = view.findViewById(R.id.mood_time);

            moodState.setText(moodEvent.getMood().toString());
            moodSocial.setText(moodEvent.getSocial().toString());
            moodTrigger.setText(moodEvent.getDescription());

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

            moodDate.setText(dateFormat.format(moodEvent.getDateTime()));
            moodTime.setText(timeFormat.format(moodEvent.getDateTime()));

            if (moodEvent.getBase64image() != null && !moodEvent.getBase64image().isEmpty()) {
                moodImage.setImageBitmap(base64ToBitmap(moodEvent.getBase64image()));
            } else {
                moodImage.setImageResource(R.drawable.cat_image);
            }

            dialog = builder.setView(view)
                    .setTitle("Mood Event Details")
                    .setNegativeButton("Close", null)
                    .create();
            Log.d(TAG, "Dialog created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating dialog: " + e.getMessage());
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            dialog = builder.setMessage("An error occurred while creating the dialog.")
                    .setPositiveButton("OK", null)
                    .create();
        }

        return dialog;
    }

    private Bitmap base64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
