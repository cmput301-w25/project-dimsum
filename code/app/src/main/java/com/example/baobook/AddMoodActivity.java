package com.example.baobook;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddMoodActivity extends AppCompatActivity {

    private final Calendar selectedDateTime = Calendar.getInstance(); // Combines date and time
    private final Calendar currentDateTime = Calendar.getInstance(); // Stores the current time at launch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood_event_fragment);

        Spinner editMood = findViewById(R.id.mood_spinner);
        TextView textDate = findViewById(R.id.text_date);
        TextView textTime = findViewById(R.id.text_time);
        TextView editDescription = findViewById(R.id.edit_description);
        Spinner editSocial = findViewById(R.id.social_situation);

        // Pre-fill Date & Time fields
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        textDate.setText(dateFormat.format(selectedDateTime.getTime()));
        textTime.setText(timeFormat.format(selectedDateTime.getTime()));

        // Date Picker
        textDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(year, month, dayOfMonth);
                        if (selectedDateTime.after(currentDateTime)) {
                            Toast.makeText(this, "Cannot select a future date", Toast.LENGTH_SHORT).show();
                            selectedDateTime.setTime(currentDateTime.getTime());
                        }
                        textDate.setText(dateFormat.format(selectedDateTime.getTime()));
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMaxDate(currentDateTime.getTimeInMillis());
            datePickerDialog.show();
        });

        // Time Picker
        textTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);

                        if (selectedDateTime.after(currentDateTime)) {
                            Toast.makeText(this, "Cannot select a future time", Toast.LENGTH_SHORT).show();
                            selectedDateTime.setTime(currentDateTime.getTime());
                        }
                        textTime.setText(timeFormat.format(selectedDateTime.getTime()));
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Save Button
        findViewById(R.id.save_button).setOnClickListener(v -> {
            try {
                Mood mood = Mood.fromString(editMood.getSelectedItem().toString());
                String dateStr = textDate.getText().toString().trim();
                String timeStr = textTime.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                String social = editSocial.getSelectedItem().toString();

                if (dateStr.equals("Select Date") || timeStr.equals("Select Time")) {
                    Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date date = selectedDateTime.getTime();
                Date time = selectedDateTime.getTime(); // Use java.util.Date for time

                if (selectedDateTime.after(currentDateTime)) {
                    Toast.makeText(this, "Date and time cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate a unique ID for the MoodEvent
                String id = UUID.randomUUID().toString();

                // Create the MoodEvent with the generated ID
                MoodEvent moodEvent = new MoodEvent(id, mood, date, time, description, social);

                // Pass the MoodEvent back to MoodHistory
                Intent resultIntent = new Intent();
                resultIntent.putExtra("moodEvent", moodEvent);
                setResult(RESULT_OK, resultIntent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());

        initializeSpinner(editMood);
    }

    private void initializeSpinner(Spinner spinner) {
        List<String> moodOptionsList = Arrays.asList(MoodUtils.MOOD_OPTIONS);
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(
                this,
                android.R.layout.simple_spinner_item,
                moodOptionsList,
                MoodUtils.MOOD_COLORS,
                MoodUtils.MOOD_EMOJIS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set default selection
        if (!moodOptionsList.isEmpty()) {
            spinner.setSelection(0);
        }
    }
}