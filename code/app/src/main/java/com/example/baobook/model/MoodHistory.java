package com.example.baobook.model;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.AddMoodActivity;
import com.example.baobook.EditFragment;
import com.example.baobook.MoodEventArrayAdapter;
import com.example.baobook.MoodEventOptionsFragment;
import com.example.baobook.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class MoodHistory extends AppCompatActivity implements
        MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener
{

    private FloatingActionButton addButton;
    private static final MoodHistoryManager moodManager = MoodHistoryManager.getInstance();
    private MoodEventArrayAdapter moodArrayAdapter;
    private ListView moodList;
    private ArrayList<MoodEvent> filteredList;

    @Override
    public void onEditMoodEvent(MoodEvent mood) {
        EditFragment fragment = new EditFragment(mood);
        fragment.show(getSupportFragmentManager(), "Edit Mood");
    }

    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
        ArrayList<MoodEvent> dataList = moodManager.getMoodList();
        for (int i = 0; i < dataList.size(); i++) {
            MoodEvent mood = dataList.get(i);
            if (mood.getDate().equals(updatedMoodEvent.getDate()) &&
                    mood.getTime().equals(updatedMoodEvent.getTime())) {
                dataList.set(i, updatedMoodEvent);
                moodArrayAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Mood Event updated!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @Override
    public void onDeleteMoodEvent(MoodEvent mood) {
        moodManager.getMoodList().remove(mood);
        moodArrayAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show();
    }

    private final ActivityResultLauncher<Intent> addMoodLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the new mood event from AddMoodActivity
                    MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                    if (mood != null) {
                        // Add mood to manager
                        moodManager.addMood(mood);
                        // Sort the list in reverse chronological order
                        moodManager.sortByDate();
                        // Notify adapter to refresh ListView
                        moodArrayAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        // Initialize ListView and Adapter
        moodList = findViewById(R.id.mood_history_list);
        filteredList = new ArrayList<>(moodManager.getMoodList()); // Initialize with all moods
        moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
        moodList.setAdapter(moodArrayAdapter);

        // Set up the mood filter spinner
        Spinner moodFilterSpinner = findViewById(R.id.mood_filter_spinner);
        ArrayAdapter<Mood> spinnerAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, Mood.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodFilterSpinner.setAdapter(spinnerAdapter);

        // Set up filter button
        Button applyFilterButton = findViewById(R.id.apply_filter_button);
        applyFilterButton.setOnClickListener(v -> {
            Mood selectedMood = (Mood) moodFilterSpinner.getSelectedItem();
            filteredList = moodManager.filterByMood(selectedMood);
            moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
            moodList.setAdapter(moodArrayAdapter);
            Toast.makeText(this, "Filtered by " + selectedMood.toString(), Toast.LENGTH_SHORT).show();
        });

        // Set up clear filter button
        Button clearFilterButton = findViewById(R.id.clear_filter_button);
        clearFilterButton.setOnClickListener(v -> {
            filteredList = new ArrayList<>(moodManager.getMoodList());
            moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
            moodList.setAdapter(moodArrayAdapter);
            moodFilterSpinner.setSelection(0);
            Toast.makeText(this, "Filter cleared", Toast.LENGTH_SHORT).show();
        });

        // Sort the mood history before displaying
        moodManager.sortByDate();

        // Notify adapter of any new moods (useful when returning to this activity)
        moodArrayAdapter.notifyDataSetChanged();

        // Floating Action Button to add a new mood
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            // Launch AddMoodActivity
            Intent intent = new Intent(MoodHistory.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

        moodList.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selectedMoodEvent = moodManager.getMoodList().get(position);
            MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(selectedMoodEvent);
            fragment.show(getSupportFragmentManager(), "MovieOptionsDialog");
        });
    }

    // Getter method to access moodList from Home or other activities
    public static ArrayList<MoodEvent> getDataList() {
        return moodManager.getMoodList();
    }
}
