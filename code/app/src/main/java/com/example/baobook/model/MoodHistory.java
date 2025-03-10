package com.example.baobook.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.baobook.AddMoodActivity;
import com.example.baobook.EditFragment;
import com.example.baobook.FilterDialogFragment;
import com.example.baobook.Home;
import com.example.baobook.Map;
import com.example.baobook.MoodEventArrayAdapter;
import com.example.baobook.MoodEventOptionsFragment;
import com.example.baobook.R;
import com.example.baobook.UserProfileActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Activity that displays the mood history list.
 * Loads from Firestore, adds each MoodEvent to MoodHistoryManager,
 * then uses manager.getFilteredList(...) to apply filters for display.
 */
public class MoodHistory extends AppCompatActivity
        implements MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener,
        FilterDialogFragment.OnFilterSaveListener {

    private FloatingActionButton addButton;
    private ListView moodList;

    private Button openFilterButton, clearAllButton;
    private LinearLayout activeFiltersContainer;

    private FirebaseFirestore db;
    private CollectionReference moodsRef;

    private MoodEventArrayAdapter moodArrayAdapter;
    private Button homeButton, mapButton, profileButton;

    // Filter states
    private Mood filterMood = null;
    private boolean filterRecentWeek = false;
    private String filterWord = null;

    // We'll display the "filtered" results in memory, for the ListView
    // So we keep a local list for quick adaptation to the UI
    private ArrayList<MoodEvent> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        db = FirebaseFirestore.getInstance();
        moodsRef = db.collection("moodEvents");

        // Initialize views
        moodList = findViewById(R.id.mood_history_list);
        addButton = findViewById(R.id.add_button);
        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        profileButton = findViewById(R.id.profile_button);
        openFilterButton = findViewById(R.id.open_filter_button);
        clearAllButton = findViewById(R.id.clear_all_button);
        activeFiltersContainer = findViewById(R.id.active_filters_container);

        // Setup the list adapter
        moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
        moodList.setAdapter(moodArrayAdapter);

        // Load data from Firestore -> push into manager
        loadMoodsFromFirestore();

        // Add new mood
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

        // If user clicks an item -> edit/delete
        moodList.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selectedMoodEvent = filteredList.get(position);
            MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(selectedMoodEvent);
            fragment.show(getSupportFragmentManager(), "MoodOptionsDialog");
        });

        // Filter button -> open dialog
        openFilterButton.setOnClickListener(v -> {
            FilterDialogFragment dialog = new FilterDialogFragment(this);
            dialog.setExistingFilters(filterMood, filterRecentWeek, filterWord);
            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });

        // Clear All -> remove filters
        clearAllButton.setOnClickListener(v -> {
            filterMood = null;
            filterRecentWeek = false;
            filterWord = null;
            applyFilters();
            Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
        });

        // Bottom nav
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, Home.class);
            startActivity(intent);
        });
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, Map.class);
            startActivity(intent);
        });
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onFilterSave(Mood mood, boolean lastWeek, String word) {
        this.filterMood = mood;
        this.filterRecentWeek = lastWeek;
        this.filterWord = word;
        applyFilters();
    }

    /**
     * Calls the manager to get a filtered list, updates the UI with results,
     * and rebuilds the filter “chips”.
     */
    private void applyFilters() {
        MoodHistoryManager manager = MoodHistoryManager.getInstance();
        ArrayList<MoodEvent> temp = manager.getFilteredList(filterMood, filterRecentWeek, filterWord);

        filteredList.clear();
        filteredList.addAll(temp);
        moodArrayAdapter.notifyDataSetChanged();

        rebuildActiveFiltersChips();
    }

    // Show “chips” for each active filter
    private void rebuildActiveFiltersChips() {
        activeFiltersContainer.removeAllViews();

        if (filterMood != null) {
            activeFiltersContainer.addView(
                    createChip(filterMood.toString(), v -> {
                        filterMood = null;
                        applyFilters();
                    })
            );
        }

        if (filterRecentWeek) {
            activeFiltersContainer.addView(
                    createChip("Last 7 days", v -> {
                        filterRecentWeek = false;
                        applyFilters();
                    })
            );
        }

        if (filterWord != null && !filterWord.isEmpty()) {
            activeFiltersContainer.addView(
                    createChip("Word: " + filterWord, v -> {
                        filterWord = null;
                        applyFilters();
                    })
            );
        }
    }

    private View createChip(String text, View.OnClickListener onRemove) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = new TextView(this);
        txt.setText(text + "  ");
        layout.addView(txt);

        TextView x = new TextView(this);
        x.setText("X");
        x.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        x.setOnClickListener(onRemove);
        layout.addView(x);

        layout.setPadding(16, 8, 16, 8);
        return layout;
    }

    /**
     * Load from Firestore, clear the manager’s list, add each mood,
     * then show them in the UI with no filters initially.
     */
    private void loadMoodsFromFirestore() {
        moodsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error loading moods", error);
                Toast.makeText(this, "Failed to load moods.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                // Clear manager first
                MoodHistoryManager manager = MoodHistoryManager.getInstance();
                manager.clearMoods();

                // Add each doc
                for (QueryDocumentSnapshot snapshot : value) {
                    MoodEvent mood = snapshot.toObject(MoodEvent.class);
                    mood.setId(snapshot.getId());
                    manager.addMood(mood);
                }

                // Sort manager’s list by date
                manager.sortByDate();

                // By default, no filters => show entire list
                filteredList.clear();
                filteredList.addAll(manager.getMoodList());
                moodArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private final ActivityResultLauncher<Intent> addMoodLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                            if (mood != null) {
                                addMoodToFirestore(mood);
                            }
                        }
                    }
            );

    private void addMoodToFirestore(MoodEvent mood) {
        moodsRef.add(mood)
                .addOnSuccessListener(docRef -> {
                    mood.setId(docRef.getId());
                    Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding mood", e);
                    Toast.makeText(this, "Failed to add mood.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditMoodEvent(MoodEvent mood) {
        EditFragment fragment = new EditFragment(mood);
        fragment.show(getSupportFragmentManager(), "Edit Mood");
    }

    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
        moodsRef.document(updatedMoodEvent.getId()).set(updatedMoodEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show();
                    loadMoodsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating mood", e);
                    Toast.makeText(this, "Failed to update mood.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteMoodEvent(MoodEvent mood) {
        moodsRef.document(mood.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show();
                    loadMoodsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting mood", e);
                    Toast.makeText(this, "Failed to delete mood.", Toast.LENGTH_SHORT).show();
                });
    }
}
