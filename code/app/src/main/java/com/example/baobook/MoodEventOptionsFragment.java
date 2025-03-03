package com.example.baobook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MoodEventOptionsFragment extends DialogFragment {
    private static final String TAG = "MoodOptionsFragment";

    private MoodEvent mood;
    private MoodEventOptionsDialogListener listener;

    public interface MoodEventOptionsDialogListener {
        void onEditMoodEvent(MoodEvent mood);
        void onDeleteMoodEvent(MoodEvent mood);
    }

    public MoodEventOptionsFragment(MoodEvent clickedMood) {
        this.mood = clickedMood;
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
            View view = inflater.inflate(R.layout.mood_event_options_fragment, null);

            Button editButton = view.findViewById(R.id.button_edit_mood);
            Button deleteButton = view.findViewById(R.id.button_delete_mood);

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditMoodEvent(mood);
                    dismiss();
                } else {
                    Log.e(TAG, "Listener is null");
                    Toast.makeText(getActivity(), "Error: Listener is null", Toast.LENGTH_SHORT).show();
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteMoodEvent(mood);
                    dismiss();
                } else {
                    Log.e(TAG, "Listener is null");
                    Toast.makeText(getActivity(), "Error: Listener is null", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setView(view)
                    .setTitle("MoodEvent Options")
                    .setNegativeButton("Cancel", null);

        } catch (Exception e) {
            Log.e(TAG, "Error creating dialog: " + e.getMessage());
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            builder.setMessage("An error occurred while creating the dialog.")
                    .setPositiveButton("OK", null);
        }
        return builder.create();
    }
}
