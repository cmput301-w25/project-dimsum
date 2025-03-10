package com.example.baobook;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
/*this activity adds a mood event with the required parameters and firebase functionality
ISSUES: still using a default username with IDK, need to grab the unqiue username later on and filter list view using unqiue user.
 */
public class AddMoodActivity extends AppCompatActivity {


    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private ImageView capImage;
    private ImageButton cameraButton;
    private Bitmap capturedImage;
    private StorageReference storageRef;


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

        capImage = findViewById(R.id.captured_image);
        cameraButton = findViewById(R.id.openCamera);
        storageRef = FirebaseStorage.getInstance().getReference();

        // Request Camera Permission Only If Not Granted
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);

         
        }*/

        MoodEvent moodEvent = (MoodEvent) getIntent().getSerializableExtra("moodEvent");
        if (moodEvent != null && moodEvent.getBase64image() != null) {  // Check if moodEvent is not null first
            capImage.setImageBitmap(base64ToBitmap(moodEvent.getBase64image()));
        } else {
            capImage.setImageResource(R.drawable.cat_image);  // Ensure default_image exists
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
                            Snackbar.make(textDate, "Cannot select a future date", Snackbar.LENGTH_LONG).show();

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
                            Snackbar.make(textDate, "Cannot select a future time", Snackbar.LENGTH_LONG).show();

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

                if (!isValidDescription(description)) {
                    Toast.makeText(this, "Trigger must be at most 20 chars or 3 words", Toast.LENGTH_SHORT).show();
                    return;
                }

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

                String id = UUID.randomUUID().toString();
                String username = "idk"; //CHANGE LATER

                String base64Image = null;
                if (capturedImage != null) {
                    base64Image = bitmapToBase64(capturedImage);
                    if (base64Image == null) {
                        return; // Image was too large, don't save it
                    }
                }
                // If a photo is captured, upload it first
                saveMoodEvent(username, id, mood, date, time, description, social, base64Image);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());

        initializeSpinner(editMood);
    }
    private void saveMoodEvent(String username, String id, Mood mood, Date date, Date time,
                               String description, String social, String base64Image) {
        if (date == null || time == null) {
            Toast.makeText(this, "Error: Mood event must have a valid date and time.", Toast.LENGTH_SHORT).show();
            return;
        }
        //where you use default image and no photo taken
        if (base64Image == null) {
            base64Image = "";
        }
        MoodEvent moodEvent = new MoodEvent(username, id, mood, date, time, description, social, base64Image);

        FirebaseFirestore.getInstance()
                .collection("moodEvents")
                .document(id)
                .set(moodEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Mood event saved!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("moodEvent", moodEvent);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save mood event", Toast.LENGTH_SHORT).show()
                );
    }

    private boolean isValidDescription(String desc) {
        if (desc.isEmpty()) return true;
        return desc.length() <= 20 && desc.trim().split("\\s+").length <= 3;
    }
    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
    private String bitmapToBase64(Bitmap bitmap) {
        int maxSize = 65536; // 64 KB limit
        int quality = 100; // Start with highest quality
        Bitmap resizedBitmap = resizeBitmap(bitmap, 512, 512); // Resize to max 512x512

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] byteArray = baos.toByteArray();

        // Reduce quality if image exceeds the 64 KB limit
        while (byteArray.length > maxSize && quality > 10) {
            baos.reset();
            quality -= 10; // Decrease quality by 10%
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byteArray = baos.toByteArray();
        }

        // If still too large, notify the user
        if (byteArray.length > maxSize) {
            Toast.makeText(this, "Image is too large! Try capturing a smaller one.", Toast.LENGTH_SHORT).show();
            return null; // Return null so we don't save it
        }

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return null;
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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

