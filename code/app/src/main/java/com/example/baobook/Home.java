package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodHistoryManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
//home activity where users will be able to see their following mood events and add new ones
public class Home extends AppCompatActivity {

    // Firestore instance and reference
    private FirebaseFirestore db;
    private CollectionReference moodsRef;
    private MoodEventHelper moodEventHelper = new MoodEventHelper();

    // ActivityResultLauncher to handle the result from AddMoodActivity
    private final ActivityResultLauncher<Intent> addMoodLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                            if (mood != null) {
                                Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        moodsRef = db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            if (v != null) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });

        // Floating Action Button to add a new mood
        FloatingActionButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            // Launch AddMoodActivity
            Intent intent = new Intent(Home.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

        // Profile button
        Button profile = findViewById(R.id.profile_button);
        profile.setOnClickListener(v -> {
            // Handle profile button click
            Intent intent = new Intent(Home.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void addMoodToFirestore(MoodEvent mood) {
        moodsRef.add(mood) // Use Firestore's auto-generated ID
                .addOnSuccessListener(documentReference -> {
                    // Set the Firestore document ID in the MoodEvent
                    mood.setId(documentReference.getId());
                    // Add the mood to the local list in MoodHistory
                    MoodHistoryManager.getInstance().addMood(mood);
                    Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to add mood.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding mood", e);
                });
    }
}