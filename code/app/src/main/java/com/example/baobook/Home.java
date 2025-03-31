package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodFilterState;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


/**
 * home activity where users will be able to see their following mood events, add new ones, and comment on existing ones.
 * The User can access their User profile, the map, and search for other users from this page
 */

public class Home extends AppCompatActivity {

    // Firestore instance and reference
    private FirebaseFirestore db;
    private CollectionReference moodsRef;
    private MoodEventHelper moodEventHelper = new MoodEventHelper();
    private FirestoreHelper firestoreHelper = new FirestoreHelper();
    private MoodEventArrayAdapter moodArrayAdapter;
    private ArrayList<MoodEvent> moodEventsList = new ArrayList<>();
    private ArrayList<MoodEvent> fullFollowingMoodEvents = new ArrayList<>();
    private MoodFilterState filterState = new MoodFilterState();
    private LinearLayout activeFiltersContainer;


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

        activeFiltersContainer = findViewById(R.id.active_filters_container);


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
        ImageButton mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, MapsActivity.class);
            startActivity(intent);
        });

        // Profile button
        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, UserProfileActivity.class);
            startActivity(intent);
        });

        // Search button
        Button searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, SearchActivity.class);
            startActivity(intent);
        });



        MaterialButton filterButton = findViewById(R.id.btn_filter);
        filterButton.setOnClickListener(v -> {
            FilterDialogFragment dialog = new FilterDialogFragment((mood, lastWeek, word) -> {
                filterState.setMood(mood);
                filterState.setRecentWeek(lastWeek);
                filterState.setWord(word);
                loadAllFollowingMoodEvents(); // now load all data to filter from
            });
            dialog.setExistingFilters(filterState.getMood(), filterState.isRecentWeek(), filterState.getWord());
            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });

        Button clearFilters = findViewById(R.id.clear_all_button);
        clearFilters.setOnClickListener(v -> {
            filterState.clear();
            applyFilters();
            Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
        });

        loadRecentFollowingMoodEvents();
    }

    private void loadAllFollowingMoodEvents() {
        String username = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", null);
        if (username == null) return;

        moodEventHelper.getAllFollowingMoodEvents(
                username,
                moodEvents -> {
                    fullFollowingMoodEvents.clear();
                    fullFollowingMoodEvents.addAll(moodEvents);
                    applyFilters();
                },
                e -> {
                    Log.e("Home", "Failed to load all following mood events", e);
                    Toast.makeText(this, "Failed to load mood events", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void loadRecentFollowingMoodEvents() {
        String username = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", null);
        if (username == null) return;

        moodEventHelper.getRecentFollowingMoodEvents(
                username,
                moodEvents -> {
                    moodEventsList.clear();
                    moodEventsList.addAll(moodEvents);
                    moodArrayAdapter.notifyDataSetChanged();
                    rebuildActiveFiltersChips();
                },
                e -> {
                    Log.e("Home", "Failed to load recent following mood events", e);
                    Toast.makeText(this, "Failed to load recent mood events", Toast.LENGTH_SHORT).show();
                }
        );
    }


    private void applyFilters() {
        if (filterState.getMood() == null && !filterState.isRecentWeek() && (filterState.getWord() == null || filterState.getWord().isEmpty())) {
            // No filters → revert to recent 3-per-user
            loadRecentFollowingMoodEvents();
            return;
        }

        // Filters are active → use all data
        moodEventsList.clear();
        moodEventsList.addAll(filterState.applyFilters(new ArrayList<>(fullFollowingMoodEvents)));
        moodArrayAdapter.notifyDataSetChanged();
        rebuildActiveFiltersChips();
    }

    private void rebuildActiveFiltersChips() {
        activeFiltersContainer.removeAllViews();

        Mood mood = filterState.getMood();
        boolean recent = filterState.isRecentWeek();
        String word = filterState.getWord();

        if (mood != null) addFilterChip("Mood: " + mood.toString(), () -> {
            filterState.setMood(null);
            applyFilters();
        });

        if (recent) addFilterChip("Last 7 Days", () -> {
            filterState.setRecentWeek(false);
            applyFilters();
        });

        if (word != null && !word.isEmpty()) addFilterChip("Word: " + word, () -> {
            filterState.setWord(null);
            applyFilters();
        });
    }

    private void addFilterChip(String label, Runnable onRemove) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView text = new TextView(this);
        text.setText(label + "  ");
        layout.addView(text);

        TextView remove = new TextView(this);
        remove.setText("X");
        remove.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        remove.setOnClickListener(v -> onRemove.run());
        layout.addView(remove);

        layout.setPadding(16, 8, 16, 8);
        activeFiltersContainer.addView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentFollowingMoodEvents(); // Refresh the list when returning to this activity
    }
}