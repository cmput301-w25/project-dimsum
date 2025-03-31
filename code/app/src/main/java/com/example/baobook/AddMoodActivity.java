package com.example.baobook;

import static com.example.baobook.EditFragment.isValidDescription;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.location.Location;


import com.example.baobook.controller.FirestoreHelper;

import com.example.baobook.adapter.MoodSpinnerAdapter;

import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.PendingAction;
import com.example.baobook.controller.PendingActionManager;
import com.example.baobook.model.Privacy;
import com.example.baobook.util.MoodUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.example.baobook.util.LocationHelper;
import com.example.baobook.util.NetworkUtil;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.GeoPoint;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.SocialSetting;
import com.example.baobook.util.UserSession;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
/*this activity adds a mood event with the required parameters and firebase functionality
ISSUES: still using a default username with IDK, need to grab the unqiue username later on and filter list view using unqiue user.
 */
public class AddMoodActivity extends AppCompatActivity {
    private ImageView capImage;
    private ImageButton cameraButton;
    private Bitmap capturedImage;
    private StorageReference storageRef;

    private MoodEventHelper moodEventHelper = new MoodEventHelper();
    private LocalDateTime selectedDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()); // Combines date and time
    private LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()); // Combines date and time
    private Location userLocation;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
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
        setContentView(R.layout.add_mood_event_fragment);
        UserSession session = new UserSession(this);
        String username = session.getUsername();

        Spinner editMood = findViewById(R.id.mood_spinner);
        TextView textDate = findViewById(R.id.text_date);
        TextView textTime = findViewById(R.id.text_time);
        TextView editDescription = findViewById(R.id.edit_description);
        SwitchCompat privateSwitch = findViewById(R.id.privacySwitch);
        SwitchCompat locationSwitch = findViewById(R.id.locationSwitch);

        //hide status bar at the top
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        capImage = findViewById(R.id.captured_image);
        cameraButton = findViewById(R.id.openCamera);
        storageRef = FirebaseStorage.getInstance().getReference();

        // Request Camera Permission Only If Not Granted
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);


        }*/




        MoodEvent currentEvent = (MoodEvent) getIntent().getSerializableExtra("moodEvent");
        if (currentEvent != null && currentEvent.getBase64image() != null) {  // Check if current moodEvent is not null first
            capImage.setImageBitmap(base64ToBitmap(currentEvent.getBase64image()));
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
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

        textDate.setText(dateFormat.format(selectedDateTime));
        textTime.setText(timeFormat.format(selectedDateTime));


        // Date Picker
        textDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime = selectedDateTime
                                .withYear(year)
                                .withMonth(month + 1)  // DatePickerDialog is 0-11 based, but LocalDateTime is 1-12 based.
                                .withDayOfMonth(dayOfMonth);

                        if (selectedDateTime.isAfter(LocalDateTime.now())) {
                            Snackbar.make(textDate, "Cannot select a date from the future", Snackbar.LENGTH_LONG).show();
                            selectedDateTime = LocalDateTime.now();
                        }
                        textDate.setText(dateFormat.format(selectedDateTime));
                    },
                    selectedDateTime.getYear(),
                    selectedDateTime.getMonth().getValue(),
                    selectedDateTime.getDayOfMonth()
            );
            datePickerDialog.getDatePicker().setMaxDate(
                    LocalDateTime
                            .now()
                            .toLocalDate()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
            );
            datePickerDialog.show();
        });

        // Time Picker
        textTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedDateTime = selectedDateTime.withHour(hourOfDay).withMinute(minute);

                        if (selectedDateTime.isAfter(LocalDateTime.now())) {
                            Snackbar.make(textDate, "Cannot select a time from the future", Snackbar.LENGTH_LONG).show();
                            selectedDateTime = LocalDateTime.now();
                        }
                        textTime.setText(timeFormat.format(selectedDateTime));
                    },
                    selectedDateTime.getHour(),
                    selectedDateTime.getMinute(),
                    false
            );
            timePickerDialog.show();
        });
        privateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { //checked = private
                privateSwitch.setText("Make post public?");
            } else {
                privateSwitch.setText("Make post private?");
            }
        });

        //location switch
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    LocationHelper.getCurrentLocation(this, new LocationHelper.LocationResultCallback() {
                        @Override
                        public void onLocationResult(double latitude, double longitude) {
                            userLocation = new Location("");
                            userLocation.setLatitude(latitude);
                            userLocation.setLongitude(longitude);
                        }

                        @Override
                        public void onLocationError(String error) {
                            Toast.makeText(AddMoodActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                userLocation = null;
            }
        });





        // Save Button
        findViewById(R.id.save_button).setOnClickListener(v -> {
            try {
                Mood mood = Mood.fromString(editMood.getSelectedItem().toString());
                String dateStr = textDate.getText().toString().trim();
                String timeStr = textTime.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                SocialSetting social = SocialSetting.fromString(editSocial.getSelectedItem().toString());
                Privacy privacy = privateSwitch.isChecked() ? Privacy.PRIVATE : Privacy.PUBLIC;
                if (!isValidDescription(description)) {
                    Toast.makeText(this, "Trigger must be at most 200 chars", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dateStr.equals("Select Date") || timeStr.equals("Select Time")) {
                    Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedDateTime.isAfter(currentDateTime)) {
                    Toast.makeText(this, "Date and time cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = UUID.randomUUID().toString();

                String base64Image = null;
                if (capturedImage != null) {
                    base64Image = bitmapToBase64(capturedImage);
                    if (base64Image == null) {
                        return; // Image was too large, don't save it
                    }
                }
                // If a photo is captured, upload it first
                // Create the MoodEvent with the generated ID
                MoodEvent moodEvent = new MoodEvent(username, id, mood, selectedDateTime.atOffset(ZoneOffset.UTC), description, social, base64Image,privacy);


                if (userLocation != null) {
                    moodEvent.setLocation(new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude()));
                }

                if (NetworkUtil.isNetworkAvailable(this)) {
                    moodEventHelper.publishMood(moodEvent,
                            aVoid -> {
                                FirestoreHelper.updateUserExpAndLevel(username, 5, // Gain 5 XP
                                        unused -> {
                                            // Fetch updated user data to show toast
                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(username)
                                                    .get()
                                                    .addOnSuccessListener(snapshot -> {
                                                        if (snapshot.exists()) {
                                                            int newExp = snapshot.getLong("exp").intValue();
                                                            int newLevel = snapshot.getLong("level").intValue();
                                                            int expNeeded = snapshot.getLong("expNeeded").intValue();

                                                            Toast.makeText(this, "You gained 5 XP!", Toast.LENGTH_SHORT).show();

                                                            if (newExp == 0) {
                                                                // If EXP reset to 0, a level-up occurred!
                                                                Toast.makeText(this, "🎉 You leveled up to Level " + newLevel + "!", Toast.LENGTH_LONG).show();
                                                            }

                                                            // Optionally update session

                                                            session.setLevel(newLevel);
                                                            session.setExp(newExp);
                                                            session.setExpNeeded(expNeeded);

                                                            Toast.makeText(this, "Mood event saved!", Toast.LENGTH_SHORT).show();
                                                            Intent resultIntent = new Intent();
                                                            resultIntent.putExtra("moodEvent", moodEvent);
                                                            resultIntent.putExtra("xpGained", true);  // or include XP value
                                                            resultIntent.putExtra("leveledUp", newExp == 0);
                                                            resultIntent.putExtra("newLevel", newLevel);
                                                            setResult(RESULT_OK, resultIntent);
                                                            finish();
                                                        }
                                                    });
                                        },
                                        error -> Log.e("XP", "Failed to add XP", error)
                                );


                            },
                            e -> Toast.makeText(this, "Failed to save mood event", Toast.LENGTH_SHORT).show()
                    );
                }
                else {
                    PendingActionManager.addAction(new PendingAction(PendingAction.ActionType.ADD, moodEvent));
                    Toast.makeText(this, "Saved offline. Will sync later.", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("MoodEvent", moodEvent);
                    setResult(RESULT_OK, resultIntent);
                    finish();

                }


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());

        initializeSpinner(editMood);
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

