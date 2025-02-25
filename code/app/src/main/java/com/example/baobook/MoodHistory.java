package com.example.baobook;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class MoodHistory extends AppCompatActivity implements AddFragment.AddMoodEventDialogListener {

    private ArrayList<MoodEvent> dataList;
    private MoodEventArrayAdapter moodArrayAdapter;
    private ListView moodList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        // Initialize List and Adapter
        dataList = new ArrayList<>();
        moodList = findViewById(R.id.mood_history_list);
        moodArrayAdapter = new MoodEventArrayAdapter(this, dataList);
        moodList.setAdapter(moodArrayAdapter);

        // Floating Action Button to add a new mood
        FloatingActionButton fab = findViewById(R.id.add_button);
        fab.setOnClickListener(v -> new AddFragment().show(getSupportFragmentManager(), "Add Mood"));
    }

    @Override
    public void addMoodEvent(MoodEvent mood) {
        if (mood != null) {
            dataList.add(mood);
            //dataList.add(new MoodEvent("Happy", new Date(), new Time(System.currentTimeMillis()), "Testing"));

            moodArrayAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
        }
    }
}
