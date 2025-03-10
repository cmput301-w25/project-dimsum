package com.example.baobook;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;

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

    private boolean isValidDescription(String desc) {
        if (desc.isEmpty()) return true;
        return desc.length() <= 20 && desc.trim().split("\\s+").length <= 3;
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
        Spinner editMood = view.findViewById(R.id.edit_mood_spinner);
        TextView editDate = view.findViewById(R.id.text_date);
        TextView editTime = view.findViewById(R.id.text_time);
        EditText editDescription = view.findViewById(R.id.edit_description);
        Spinner editSocial = view.findViewById(R.id.social_spinner);

        // Initialize the Spinner with MoodUtils
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(
                context,
                android.R.layout.simple_spinner_item,
                Arrays.asList(MoodUtils.MOOD_OPTIONS),
                MoodUtils.MOOD_COLORS,
                MoodUtils.MOOD_EMOJIS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editMood.setAdapter(adapter);

        if (moodEvent != null) {
            int position = adapter.getPosition(moodEvent.getMood().toString());
            editMood.setSelection(position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            editDate.setText(dateFormat.format(moodEvent.getDate()));
            editTime.setText(timeFormat.format(moodEvent.getTime()));
            editDescription.setText(moodEvent.getDescription());
        }

        editDate.setOnClickListener(v -> {
            new DatePickerDialog(context,
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        editDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        editTime.setOnClickListener(v -> {
            new TimePickerDialog(context,
                    (view12, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        editTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.getTime()));
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
                    true
            ).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Edit Mood Event")
                .setPositiveButton("Save", null) // Set to null for overriding later
                .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                Mood newMood = Mood.fromString(editMood.getSelectedItem().toString());
                String newDescription = editDescription.getText().toString().trim();
                String newSocial = editSocial.getSelectedItem().toString();

                if (!isValidDescription(newDescription)) {
                    Toast.makeText(getContext(), "Trigger must be at most 20 chars or 3 words", Toast.LENGTH_SHORT).show();
                    return; // Prevent dialog from closing
                }

                moodEvent.editMoodEvent(newMood, selectedDate.getTime(), selectedTime.getTime(), newDescription, newSocial);
                listener.onMoodEdited(moodEvent);
                dialog.dismiss(); // Dismiss only when validation passes
            });
        });

        return dialog;
    }
    }
