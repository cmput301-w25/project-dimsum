package com.example.baobook;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private ImageView capImage;
    private ImageButton cameraButton;
    private Bitmap capturedImage;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
                }
            });

    // ActivityResultLauncher for the camera
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        capturedImage = (Bitmap) extras.get("data"); // Store image
                        capImage.setImageBitmap(capturedImage);  // Display image
                    } else {
                        Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood_event_fragment); // Use the same layout as the fragment

        Spinner editStates = findViewById(R.id.spinner_states);
        TextView textDate = findViewById(R.id.text_date);
        TextView textTime = findViewById(R.id.text_time);
        TextView editDescription = findViewById(R.id.edit_description);
        capImage = findViewById(R.id.captured_image);
        cameraButton = findViewById(R.id.openCamera);

        // Request Camera Permission Only If Not Granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Camera Button Click Listener
        cameraButton.setOnClickListener(v -> {
            // Check and log camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("CameraDebug", "Camera permission is NOT granted!");
                Toast.makeText(this, "Please grant camera permission first.", Toast.LENGTH_SHORT).show();
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                return; // Stop execution if permission isn't granted
            } else {
                Log.d("CameraDebug", "Camera permission is granted.");
            }

            Log.d("CameraDebug", "Launching Camera...");
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


            cameraLauncher.launch(cameraIntent);

        });


        // Date Picker
        textDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
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
                    this,
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

        // Save Button
        findViewById(R.id.save_button).setOnClickListener(v -> {
            try {
                String state = editStates.getSelectedItem().toString();
                String dateStr = textDate.getText().toString().trim();
                String timeStr = textTime.getText().toString().trim();
                String description = editDescription.getText().toString().trim();

                if (dateStr.equals("Select Date") || timeStr.equals("Select Time")) {
                    Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date date = selectedDate.getTime();
                Time time = new Time(selectedTime.getTimeInMillis());

                // Create MoodEvent with state, date, time, and description
                MoodEvent mood = new MoodEvent(state, date, time, description);

                // Return the MoodEvent to the calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("moodEvent", mood);
                setResult(RESULT_OK, resultIntent);
                finish();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid data!", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel Button
        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());

        // Initialize the Spinner with MoodUtils
        initializeSpinner(editStates);
    }

    // Helper method to initialize the Spinner with MoodUtils data
    private void initializeSpinner(Spinner spinner) {
        // Convert the String[] array to a List<String>
        List<String> moodOptionsList = Arrays.asList(MoodUtils.MOOD_OPTIONS);

        // Create and set the custom adapter
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(
                this,
                android.R.layout.simple_spinner_item,
                moodOptionsList, // Pass the List<String> instead of the array
                MoodUtils.MOOD_COLORS,
                MoodUtils.MOOD_EMOJIS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
