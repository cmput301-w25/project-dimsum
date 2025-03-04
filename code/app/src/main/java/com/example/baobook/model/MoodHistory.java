package com.example.baobook.model;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

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

    // Static list to store moods across activities
    private static final ArrayList<MoodEvent> dataList = new ArrayList<>();
    private MoodEventArrayAdapter moodArrayAdapter;
    private ListView moodList;

    @Override
    public void onEditMoodEvent(MoodEvent mood) {
        EditFragment fragment = new EditFragment(mood);
        fragment.show(getSupportFragmentManager(), "Edit Mood");
    }

    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
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
        dataList.remove(mood);
        moodArrayAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show();
    }

    private void sortMoodHistoryByDate() {
        Collections.sort(dataList, (mood1, mood2) -> {
            int dateComparison = mood2.getDate().compareTo(mood1.getDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            // If dates are equal, compare times
            return mood2.getTime().compareTo(mood1.getTime());
        });
    }

    private final ActivityResultLauncher<Intent> addMoodLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the new mood event from AddMoodActivity
                    MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                    if (mood != null) {
                        // Add mood to static list
                        getDataList().add(mood);
                        // Sort the list in reverse chronological order
                        sortMoodHistoryByDate();
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

        // Sort the mood history before displaying
        sortMoodHistoryByDate();

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
            MoodEvent selectedMoodEvent = dataList.get(position);
            MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(selectedMoodEvent);
            fragment.show(getSupportFragmentManager(), "MovieOptionsDialog");
        });

    }

    // Getter method to access dataList from Home or other activities
    public static ArrayList<MoodEvent> getDataList() {
        return dataList;
    }
}
