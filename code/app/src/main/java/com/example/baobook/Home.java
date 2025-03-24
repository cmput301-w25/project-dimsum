package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.MoodEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

//home activity where users will be able to see their following mood events and add new ones
public class Home extends AppCompatActivity {

    // Firestore instance and reference
    private FirebaseFirestore db;
    private CollectionReference moodsRef;
    private MoodEventHelper moodEventHelper = new MoodEventHelper();
    private FirestoreHelper firestoreHelper = new FirestoreHelper();
    private MoodEventArrayAdapter moodArrayAdapter;
    private ArrayList<MoodEvent> moodEventsList = new ArrayList<>();

    // ActivityResultLauncher to handle the result from AddMoodActivity
    private final ActivityResultLauncher<Intent> addMoodLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                            if (mood != null) {
                                Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                                loadRecentFollowingMoodEvents(); // Refresh the list
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

        // Initialize ListView and Adapter
        ListView moodEventsListView = findViewById(R.id.mood_events_list);
        moodArrayAdapter = new MoodEventArrayAdapter(this, moodEventsList);
        moodEventsListView.setAdapter(moodArrayAdapter);

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

        // Map button
        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, MapsActivity.class);
            startActivity(intent);
        });
        // Profile button
        Button profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, UserProfileActivity.class);
            startActivity(intent);
        });
        Button searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(v -> {
            // Handle search button click
            Intent intent = new Intent(Home.this, SearchActivity.class);
            startActivity(intent);
        });

        // Load recent mood events from followed users
        loadRecentFollowingMoodEvents();
    }

    private void loadRecentFollowingMoodEvents() {
        // Get the current user's username from SharedPreferences
        String username = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", null);

        Log.d("Home", "Loading recent following mood events for user: " + username);

        if (username != null) {
            moodEventHelper.getRecentFollowingMoodEvents(
                username,
                moodEvents -> {
                    Log.d("Home", "Received " + moodEvents.size() + " mood events");
                    moodEventsList.clear();
                    moodEventsList.addAll(moodEvents);
                    Log.d("Home", "Updated moodEventsList. New size: " + moodEventsList.size());
                    moodArrayAdapter.notifyDataSetChanged();
                },
                e -> {
                    Log.e("Home", "Failed to load mood events", e);
                    Toast.makeText(this, "Failed to load mood events: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            );
        } else {
            Log.e("Home", "Username is null in SharedPreferences");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentFollowingMoodEvents(); // Refresh the list when returning to this activity
    }
}