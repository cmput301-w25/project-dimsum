package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**class where user can view their profile including their own mood history. Has options to view each mood,
 * delete, and edit mood events. User can still add a mood, and go to the home moodHistory
 *
 */
public class UserProfileActivity extends AppCompatActivity implements
        MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener
{

    // Static list to store moods across activities
    private static final ArrayList<MoodEvent> dataList = new ArrayList<>();
    private MoodEventArrayAdapter moodArrayAdapter;

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
        setContentView(R.layout.profile);

        // Initialize ListView and Adapter
        ListView moodList = findViewById(R.id.mood_history_list);
        moodArrayAdapter = new MoodEventArrayAdapter(this, dataList);
        moodList.setAdapter(moodArrayAdapter);

        // Notify adapter of any new moods (useful when returning to this activity)
        moodArrayAdapter.notifyDataSetChanged();

        // Floating Action Button to add a new mood
        FloatingActionButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            // Launch AddMoodActivity
            Intent intent = new Intent(UserProfileActivity.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });
        //edit mood when mood is selected
        moodList.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selectedMoodEvent = dataList.get(position);
            MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(selectedMoodEvent);
            fragment.show(getSupportFragmentManager(), "MovieOptionsDialog");
        });
        //logout option
//        Button logout = findViewById(R.id.logout_button);
//        logout.setOnClickListener(v->{
//            //launch logout activity
//            Intent intent = new Intent(UserProfileActivity.this, LogoutActivity);
//        });

    }

    // Getter method to access dataList from Home or other activities
    public static ArrayList<MoodEvent> getDataList() {
        return dataList;
    }
}

