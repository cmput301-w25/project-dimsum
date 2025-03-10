package com.example.baobook;

/*
This fragment pops up when u tap on a moond and it gives an option whether to delete or edit said mood.
After User has selected the option it will do the task accordingly (with the help of other classes).
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class MoodEventOptionsFragment extends DialogFragment {
    private static final String TAG = "MoodOptionsFragment";

    private final MoodEvent moodEvent;
    private MoodEventOptionsDialogListener listener;

    public interface MoodEventOptionsDialogListener {
        void onEditMoodEvent(MoodEvent mood);
        void onDeleteMoodEvent(MoodEvent mood);
    }

    public MoodEventOptionsFragment(MoodEvent clickedMood) {
        this.moodEvent = clickedMood;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (context instanceof MoodEventOptionsDialogListener) {
                listener = (MoodEventOptionsDialogListener) context;
            } else {
                throw new RuntimeException(context + " must implement MoodEventOptionsDialogListener");
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Error attaching listener: " + e.getMessage());
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        try {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View view = inflater.inflate(R.layout.mood_event_details, null);

            ImageView moodImage = view.findViewById(R.id.mood_image);
            TextView moodState = view.findViewById(R.id.mood_state);
            TextView moodSocial = view.findViewById(R.id.mood_social);
            TextView moodTrigger = view.findViewById(R.id.mood_trigger);
            TextView moodDate = view.findViewById(R.id.mood_date);
            TextView moodTime = view.findViewById(R.id.mood_time);

            Button editButton = view.findViewById(R.id.button_edit_mood);
            Button deleteButton = view.findViewById(R.id.button_delete_mood);

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

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditMoodEvent(moodEvent);
                    dismiss();
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteMoodEvent(moodEvent);
                    dismiss();
                }
            });

            builder.setView(view)
                    .setTitle("Mood Event Details")
                    .setNegativeButton("Cancel", null);

        } catch (Exception e) {
            Log.e(TAG, "Error creating dialog: " + e.getMessage());
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            builder.setMessage("An error occurred while creating the dialog.")
                    .setPositiveButton("OK", null);
        }
        return builder.create();
    }

    private Bitmap base64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
