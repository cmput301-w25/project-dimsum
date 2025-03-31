package com.example.baobook;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.core.content.ContextCompat;

import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodFilterState;
import com.example.baobook.controller.MoodHistoryManager;
import com.example.baobook.model.PendingAction;
import com.example.baobook.controller.PendingActionManager;
import com.example.baobook.util.NetworkUtil;
import com.example.baobook.util.UserSession;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;



/**
 * Activity that displays the mood history list.
 * Loads from Firestore, adds each MoodEvent to MoodHistoryManager,
 * then uses manager.getFilteredList(...) to apply filters for display.
 * Also has the functions for delete and edit
 */
public class MoodHistory extends AppCompatActivity
        implements MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener,
        FilterDialogFragment.OnFilterSaveListener {

    private LinearLayout activeFiltersContainer;

    private final MoodEventHelper moodEventHelper = new MoodEventHelper();
    private UserSession userSession;

    private MoodEventArrayAdapter moodArrayAdapter;

    private final MoodFilterState filterState = new MoodFilterState();

    // We'll display the "filtered" results in memory, for the ListView
    // So we keep a local list for quick adaptation to the UI
    private ArrayList<MoodEvent> filteredList = new ArrayList<>();

    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
        if (NetworkUtil.isNetworkAvailable(this)) {
            PendingActionManager.syncPendingActions(this, () -> {
                Log.d("MoodHistory", "Synced pending actions");
                loadMoodsFromFirestore(); // Refresh list
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver);
            connectivityReceiver = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        userSession = new UserSession(this);

        // Initialize views
        ListView moodList = findViewById(R.id.mood_history_list);
        FloatingActionButton addButton = findViewById(R.id.add_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton mapButton = findViewById(R.id.map_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        Button openFilterButton = findViewById(R.id.open_filter_button);
        Button clearAllButton = findViewById(R.id.clear_all_button);
        Button SyncButton = findViewById(R.id.syncButton);


        activeFiltersContainer = findViewById(R.id.active_filters_container);

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

        // Setup the list adapter
        moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
        moodList.setAdapter(moodArrayAdapter);

        // Load data from Firestore -> push into manager
        loadMoodsFromFirestore();

        SyncButton.setOnClickListener(v -> {
            filterState.clear();
            loadMoodsFromFirestore();
            Toast.makeText(this, "Mood History Synced", Toast.LENGTH_SHORT).show();
        });
        // Add new mood
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

        // Filter button -> open dialog
        openFilterButton.setOnClickListener(v -> {
            FilterDialogFragment dialog = new FilterDialogFragment(this);
            dialog.setExistingFilters(filterState.getMood(), filterState.isRecentWeek(), filterState.getWord());
            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });

        // Clear All -> remove filters
        clearAllButton.setOnClickListener(v -> {
            filterState.clear();
            applyFilters();
            Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
        });

        // Bottom nav
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, Home.class);
            startActivity(intent);
        });
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, MapsActivity.class);
            startActivity(intent);
        });
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onFilterSave(Mood mood, boolean lastWeek, String word) {
        filterState.setMood(mood);
        filterState.setRecentWeek(lastWeek);
        filterState.setWord(word);
        applyFilters();
    }

    /**
     * Calls the manager to get a filtered list, updates the UI with results,
     * and rebuilds the filter "chips".
     */
    private void applyFilters() {
        ArrayList<MoodEvent> temp = filterState.applyFilters();

        filteredList.clear();
        filteredList.addAll(temp);
        moodArrayAdapter.notifyDataSetChanged();

        rebuildActiveFiltersChips();
    }

    // Show "chips" for each active filter
    private void rebuildActiveFiltersChips() {
        Mood mood = filterState.getMood();
        boolean isRecentWeek = filterState.isRecentWeek();
        String word = filterState.getWord();

        activeFiltersContainer.removeAllViews();

        if (mood != null) {
            activeFiltersContainer.addView(
                    createChip(mood.toString(), v -> {
                        filterState.setMood(null);
                        applyFilters();
                    })
            );
        }

        if (isRecentWeek) {
            activeFiltersContainer.addView(
                    createChip("Last 7 days", v -> {
                        filterState.setRecentWeek(false);
                        applyFilters();
                    })
            );
        }

        if (word != null && !word.isEmpty()) {
            activeFiltersContainer.addView(
                    createChip("Word: " + word, v -> {
                        filterState.setWord(null);
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
     * Load from Firestore, clear the manager's list, add each mood,
     * then show them in the UI with no filters initially.
     */
    private void loadMoodsFromFirestore() {
        //skip firestore loading when offline bc otherwise list overriden
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Log.d("Offline", "Skipping Firestore load — no network");
            return;
        }

        moodEventHelper.getMoodEventsByUser(userSession.getUsername(), moodEvents -> {
            MoodHistoryManager manager = MoodHistoryManager.getInstance();
            manager.clearMoods();
            manager.addAllMoods(moodEvents);

            filteredList.clear();
            filteredList.addAll(manager.getMoodList());
            moodArrayAdapter.notifyDataSetChanged();
        }, e -> {
            Log.e("Firestore", "Error loading moods", e);
            Toast.makeText(this, "Failed to load moods.", Toast.LENGTH_SHORT).show();
        });
    }

    private final ActivityResultLauncher<Intent> addMoodLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            MoodEvent mood = (MoodEvent) result.getData().getParcelableExtra("moodEvent");
                            if (mood != null) {
                                Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                                filteredList.add(0, mood);
                                moodArrayAdapter.notifyDataSetChanged();
                                loadMoodsFromFirestore();

                            }
                        }
                    }
            );
    @Override
    public void onEditMoodEvent(MoodEvent mood) {
        EditFragment fragment = new EditFragment(mood);
        fragment.show(getSupportFragmentManager(), "Edit Mood");
    }

    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
        if (NetworkUtil.isNetworkAvailable(this)) {
            moodEventHelper.updateMood(updatedMoodEvent, aVoid -> {
                Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show();
                loadMoodsFromFirestore();
            }, e -> {
                Log.e("Firestore", "Error updating mood", e);
                Toast.makeText(this, "Failed to update mood.", Toast.LENGTH_SHORT).show();
            });
        } else {
            PendingActionManager.addAction(new PendingAction(PendingAction.ActionType.EDIT, updatedMoodEvent));
            Toast.makeText(this, "Update saved offline. Will sync later.", Toast.LENGTH_SHORT).show();
        }
        filterState.clear();
        applyFilters();
    }

    @Override
    public void onDeleteMoodEvent(MoodEvent mood) {
        if (NetworkUtil.isNetworkAvailable(this)) {
            moodEventHelper.deleteMood(mood, aVoid -> {
                Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show();
                loadMoodsFromFirestore();
            }, e -> {
                Log.e("Firestore", "Error deleting mood", e);
                Toast.makeText(this, "Failed to delete mood.", Toast.LENGTH_SHORT).show();
            });
        } else {
            PendingActionManager.addAction(new PendingAction(PendingAction.ActionType.DELETE, mood));
            Toast.makeText(this, "Delete saved offline. Will sync later.", Toast.LENGTH_SHORT).show();
        }
        filterState.clear();
        applyFilters();
    }
}
