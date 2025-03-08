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

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddMoodActivity extends AppCompatActivity {

    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private final Calendar currentDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood_event_fragment);

        Spinner editMood = findViewById(R.id.mood_spinner);
        TextView textDate = findViewById(R.id.text_date);
        TextView textTime = findViewById(R.id.text_time);
        TextView editDescription = findViewById(R.id.edit_description);
        Spinner editSocial = findViewById(R.id.social_situation);

        // Date Picker
        textDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        if (selectedDate.after(currentDateTime)) {
                            Toast.makeText(this, "Cannot select a future date", Toast.LENGTH_SHORT).show();
                            selectedDate.setTime(currentDateTime.getTime());
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        textDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMaxDate(currentDateTime.getTimeInMillis());
            datePickerDialog.show();
        });

        // Time Picker
        textTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);

                        // Ensure selected time is not in the future
                        if (selectedTime.after(currentDateTime)) {
                            Toast.makeText(this, "Cannot select a future time", Toast.LENGTH_SHORT).show();
                            selectedTime.setTime(currentDateTime.getTime());
                        }
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        textTime.setText(timeFormat.format(selectedTime.getTime()));
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
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

                Date date = selectedDate.getTime();
                Time time = new Time(selectedTime.getTimeInMillis());

                // Final check to ensure no future date/time is selected
                if (selectedDate.after(currentDateTime) || selectedTime.after(currentDateTime)) {
                    Toast.makeText(this, "Date and time cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }

                MoodEvent moodEvent = new MoodEvent(mood, date, time, description, social);

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
    }
}
