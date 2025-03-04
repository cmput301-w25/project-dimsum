package com.example.baobook;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class EditFragment extends DialogFragment {

    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private EditMoodEventDialogListener listener;
    private MoodEvent moodEvent;

    public EditFragment(MoodEvent clickedMood) {
        this.moodEvent = clickedMood;
        selectedDate.setTime(clickedMood.getDate());
        selectedTime.setTime(clickedMood.getTime());
    }

    public EditFragment() {
        super();
    }

    public interface EditMoodEventDialogListener {
        void onMoodEdited(MoodEvent updatedMoodEvent);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditMoodEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement EditMoodEventDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Fragment not attached to a context.");
        }

        View view = LayoutInflater.from(context).inflate(R.layout.edit_fragment, null);
        Spinner editStates = view.findViewById(R.id.spinner_states);
        TextView editDate = view.findViewById(R.id.text_date);
        TextView editTime = view.findViewById(R.id.text_time);
        EditText editDescription = view.findViewById(R.id.edit_description);

        // Initialize the Spinner with MoodUtils
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(
                context,
                android.R.layout.simple_spinner_item,
                Arrays.asList(MoodUtils.MOOD_OPTIONS), // Convert String[] to List<String>
                MoodUtils.MOOD_COLORS,
                MoodUtils.MOOD_EMOJIS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editStates.setAdapter(adapter);

        // Set initial values if editing an existing MoodEvent
        if (moodEvent != null) {
            int position = adapter.getPosition(moodEvent.getState());
            editStates.setSelection(position);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            editDate.setText(dateFormat.format(moodEvent.getDate()));
            editTime.setText(timeFormat.format(moodEvent.getTime()));
            editDescription.setText(moodEvent.getDescription());
        }

        // Date Picker
        editDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        editDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Time Picker
        editTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    context,
                    (view12, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        editTime.setText(timeFormat.format(selectedTime.getTime()));
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Build and return the dialog
        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Edit Mood Event")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newState = editStates.getSelectedItem().toString();
                    String newDescription = editDescription.getText().toString();

                    // Create an updated MoodEvent
                    MoodEvent updatedMood = new MoodEvent(
                            newState,
                            selectedDate.getTime(),
                            new Time(selectedTime.getTimeInMillis()),
                            newDescription
                    );

                    // Notify the listener
                    listener.onMoodEdited(updatedMood);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();
    }
}