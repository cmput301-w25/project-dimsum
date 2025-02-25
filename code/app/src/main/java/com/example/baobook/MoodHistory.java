package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MoodHistory extends AppCompatActivity {

    private FloatingActionButton addButton;

    // Static list to store moods across activities
    private static final ArrayList<MoodEvent> dataList = new ArrayList<>();
    private MoodEventArrayAdapter moodArrayAdapter;
    private ListView moodList;

    private final ActivityResultLauncher<Intent> addMoodLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the new mood event from AddMoodActivity
                    MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                    if (mood != null) {
                        // Add mood to static list
                        getDataList().add(mood);
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
        moodArrayAdapter = new MoodEventArrayAdapter(this, dataList);
        moodList.setAdapter(moodArrayAdapter);

        // Notify adapter of any new moods (useful when returning to this activity)
        moodArrayAdapter.notifyDataSetChanged();

        // Floating Action Button to add a new mood
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            // Launch AddMoodActivity
            Intent intent = new Intent(MoodHistory.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });
    }

    // Getter method to access dataList from Home or other activities
    public static ArrayList<MoodEvent> getDataList() {
        return dataList;
    }
}
