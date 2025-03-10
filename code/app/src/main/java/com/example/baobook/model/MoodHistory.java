package com.example.baobook.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class MoodHistory extends AppCompatActivity implements
        MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener {

    private FloatingActionButton addButton;
    private ListView moodList;
    private Spinner moodFilterSpinner;
    private Button applyFilterButton, clearFilterButton;

    private FirebaseFirestore db;
    private CollectionReference moodsRef;

    private static ArrayList<MoodEvent> dataList = new ArrayList<>();
    private ArrayList<MoodEvent> filteredList;
    private MoodEventArrayAdapter moodArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        moodsRef = db.collection("moodEvents");

        // Initialize views
        moodList = findViewById(R.id.mood_history_list);
        moodFilterSpinner = findViewById(R.id.mood_filter_spinner);
        applyFilterButton = findViewById(R.id.apply_filter_button);
        clearFilterButton = findViewById(R.id.clear_filter_button);
        addButton = findViewById(R.id.add_button);

        // Initialize data lists
        dataList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Initialize adapter
        moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
        moodList.setAdapter(moodArrayAdapter);

        // Load moods from Firestore
        loadMoodsFromFirestore();

        // Set up mood filter spinner
        ArrayAdapter<Mood> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Mood.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodFilterSpinner.setAdapter(spinnerAdapter);

        // Set up filter button
        applyFilterButton.setOnClickListener(v -> {
            Mood selectedMood = (Mood) moodFilterSpinner.getSelectedItem();
            filteredList = filterByMood(selectedMood);
            moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
            moodList.setAdapter(moodArrayAdapter);
            Toast.makeText(this, "Filtered by " + selectedMood.toString(), Toast.LENGTH_SHORT).show();
        });

        // Set up clear filter button
        clearFilterButton.setOnClickListener(v -> {
            filteredList = new ArrayList<>(dataList);
            moodArrayAdapter = new MoodEventArrayAdapter(this, filteredList);
            moodList.setAdapter(moodArrayAdapter);
            moodFilterSpinner.setSelection(0);
            Toast.makeText(this, "Filter cleared", Toast.LENGTH_SHORT).show();
        });

        // Floating Action Button to add a new mood
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistory.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

        // Set up item click listener for mood list
        moodList.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selectedMoodEvent = filteredList.get(position);
            MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(selectedMoodEvent);
            fragment.show(getSupportFragmentManager(), "MoodOptionsDialog");
        });
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
                    mood.setId(snapshot.getId()); // Set Firestore document ID
                    dataList.add(mood);
                }
                sortMoodHistoryByDate();
                filteredList = new ArrayList<>(dataList);
                moodArrayAdapter.notifyDataSetChanged();
            }
        });
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
                    MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("moodEvent");
                    if (mood != null) {
                        addMoodToFirestore(mood);
                    }
                }
            });

    private void addMoodToFirestore(MoodEvent mood) {
        moodsRef.add(mood) // Use Firestore's auto-generated ID
                .addOnSuccessListener(documentReference -> {
                    mood.setId(documentReference.getId()); // Set the document ID
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
                    loadMoodsFromFirestore(); // Refresh the list
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
                    loadMoodsFromFirestore(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting mood", e);
                    Toast.makeText(this, "Failed to delete mood.", Toast.LENGTH_SHORT).show();
                });
    }

    public ArrayList<MoodEvent> filterByMood(Mood moodType) {
        ArrayList<MoodEvent> filteredList = new ArrayList<>();
        for (MoodEvent mood : dataList) {
            if (mood.getMood() == moodType) {
                filteredList.add(mood);
            }
        }
        return filteredList;
    }

    public static ArrayList<MoodEvent> getDataList() {
        return dataList;
    }

}