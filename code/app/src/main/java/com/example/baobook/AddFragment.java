package com.example.baobook;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddFragment extends DialogFragment {

    interface AddMoodEventDialogListener {
        void addMoodEvent(MoodEvent mood);
    }

    private AddMoodEventDialogListener listener;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddMoodEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement AddMoodEventDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Fragment not attached to a context.");
        }

        View view = LayoutInflater.from(context).inflate(R.layout.add_mood_event_fragment, null);
        Spinner editStates = view.findViewById(R.id.spinner_states);
        TextView textDate = view.findViewById(R.id.text_date);
        TextView textTime = view.findViewById(R.id.text_time);
        TextView editDescription = view.findViewById(R.id.edit_description);

        // Date Picker
        textDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        textDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Time Picker
        textTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    context,
                    (view12, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        textTime.setText(timeFormat.format(selectedTime.getTime()));
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder
                .setView(view)
                .setTitle("Add a Mood")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        String state = editStates.getSelectedItem().toString();
                        String dateStr = textDate.getText().toString().trim();
                        String timeStr = textTime.getText().toString().trim();
                        String description = editDescription.getText().toString().trim();

                        if (dateStr.equals("Select Date") || timeStr.equals("Select Time")) {
                            Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Date date = selectedDate.getTime();
                        Time time = new Time(selectedTime.getTimeInMillis());

                        MoodEvent mood = new MoodEvent(state, date, time, description);
                        listener.addMoodEvent(mood);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Invalid data!", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
    }
}
