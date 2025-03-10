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

    private static ArrayList<MoodEvent> dataList = new ArrayList<>();
    private ArrayList<MoodEvent> filteredList;
    private MoodEventArrayAdapter moodArrayAdapter;
    private Button homeButton, mapButton, profileButton;

    // Fields to store the user’s chosen filters
    private Mood filterMood = null;          // which mood?
    private boolean filterRecentWeek = false; // last 7 days?
    private String filterWord = null;         // single word in description?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        db = FirebaseFirestore.getInstance();
        moodsRef = db.collection("moodEvents");

        // Initialize views
        moodList = findViewById(R.id.mood_history_list);
        addButton = findViewById(R.id.add_button);

        // BOTTOM NAV BUTTONS
        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        profileButton = findViewById(R.id.profile_button);

        // 2) The new filter UI elements
        openFilterButton = findViewById(R.id.open_filter_button);
        clearAllButton = findViewById(R.id.clear_all_button);
        activeFiltersContainer = findViewById(R.id.active_filters_container);

        // Prepare data
        dataList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Set up the ListView adapter
        moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
        moodList.setAdapter(moodArrayAdapter);

        // Load data from Firestore
        loadMoodsFromFirestore();

        // FAB to add new mood
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

        // If user clicks an item -> show edit/delete options
        moodList.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selectedMoodEvent = filteredList.get(position);
            MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(selectedMoodEvent);
            fragment.show(getSupportFragmentManager(), "MoodOptionsDialog");
        });

        // 3) “Filter” button: open the FilterDialogFragment
        openFilterButton.setOnClickListener(v -> {
            FilterDialogFragment dialog = new FilterDialogFragment(this);

            // Optionally pass current filters so user sees them in the dialog:
            dialog.setExistingFilters(filterMood, filterRecentWeek, filterWord);

            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });

        // 4) “Clear All” button: reset everything
        clearAllButton.setOnClickListener(v -> {
            filterMood = null;
            filterRecentWeek = false;
            filterWord = null;
            applyFilters();
            Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
        });

        // HOME button -> open HomeActivity
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, Home.class);
            startActivity(intent);
        });

        // MAP button -> open MapActivity
        // In part4

        // PROFILE button -> open ProfileActivity
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    // Called by FilterDialogFragment after user taps “Save”
    @Override
    public void onFilterSave(Mood mood, boolean lastWeek, String word) {
        // Store the chosen filters
        this.filterMood = mood;          // e.g. Mood.ANGER
        this.filterRecentWeek = lastWeek; // true/false
        this.filterWord = word;          // single word from user

        // Then apply them
        applyFilters();
    }

    // The actual filtering logic
    private void applyFilters() {
        // Start from entire data set
        ArrayList<MoodEvent> temp = new ArrayList<>(dataList);

        // 1) If user picked a mood, remove all events that aren’t that mood
        if (filterMood != null) {
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                if (me.getMood() != filterMood) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // 2) If user wants last 7 days only
        if (filterRecentWeek) {
            long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                Date dateTime = combineDateAndTime(me.getDate(), me.getTime());
                if (dateTime.getTime() < oneWeekAgo) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // 3) If user provided a single word
        if (filterWord != null && !filterWord.isEmpty()) {
            String lower = filterWord.toLowerCase();
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                String desc = (me.getDescription() == null) ? "" : me.getDescription().toLowerCase();
                if (!desc.contains(lower)) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // Sort them descending by date/time
        Collections.sort(temp, (m1, m2) -> {
            int dateComparison = m2.getDate().compareTo(m1.getDate());
            if (dateComparison != 0) return dateComparison;
            return m2.getTime().compareTo(m1.getTime());
        });

        // Replace filteredList contents
        filteredList.clear();
        filteredList.addAll(temp);
        moodArrayAdapter.notifyDataSetChanged();

        rebuildActiveFiltersChips();
    }

    // Combine date + time
    private Date combineDateAndTime(Date date, Date time) {
        return date;
    }

    // Build small “chips” for each active filter, each with an “X” to remove it
    private void rebuildActiveFiltersChips() {
        activeFiltersContainer.removeAllViews();

        // Mood chip
        if (filterMood != null) {
            activeFiltersContainer.addView(
                    createChip(filterMood.toString(), v -> {
                        filterMood = null;
                        applyFilters();
                    })
            );
        }

        // “Last 7 days” chip
        if (filterRecentWeek) {
            activeFiltersContainer.addView(
                    createChip("Last 7 days", v -> {
                        filterRecentWeek = false;
                        applyFilters();
                    })
            );
        }

        // Word chip
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

    private void loadMoodsFromFirestore() {
        moodsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error loading moods", error);
                Toast.makeText(this, "Failed to load moods.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                dataList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    MoodEvent mood = snapshot.toObject(MoodEvent.class);
                    mood.setId(snapshot.getId());
                    dataList.add(mood);
                }
                sortMoodHistoryByDate();

                // By default, filtered = entire data
                filteredList.clear();
                filteredList.addAll(dataList);
                moodArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private void sortMoodHistoryByDate() {
        Collections.sort(dataList, (m1, m2) -> {
            int dateComparison = m2.getDate().compareTo(m1.getDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return m2.getTime().compareTo(m1.getTime());
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
                    });

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

    public static ArrayList<MoodEvent> getDataList() {
        return dataList;
    }
}
