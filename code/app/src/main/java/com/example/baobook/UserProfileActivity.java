package com.example.baobook;

import static com.example.baobook.model.MoodHistory.getDataList;
import com.example.baobook.model.MoodEvent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private final ArrayList<MoodEvent> dataList = new ArrayList<>();
    private MoodEventArrayAdapter moodArrayAdapter;
    private FirebaseFirestore db;
    private String username;
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
                    MoodEvent mood = (MoodEvent) result.getData().getSerializableExtra("MoodEvent");
                    if (mood != null) {
                        // Add mood to static list
                        dataList.add(mood);
                        // Notify adapter to refresh ListView
                        moodArrayAdapter.notifyDataSetChanged();
                        FirestoreHelper.firestoreMood(mood, this);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        //get current username from shared preferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("Username", null);
        TextView usernameText = findViewById(R.id.username_text);
        usernameText.setText(username);//set username

        // Initialize ListView and Adapter
        ListView moodList = findViewById(R.id.mood_history_list);
        moodArrayAdapter = new MoodEventArrayAdapter(this, dataList);
        moodList.setAdapter(moodArrayAdapter);

// Load user moods after setting adapter
//        FirestoreHelper.loadUserMoods(dataList, moodArrayAdapter, this);

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
        Button logout = findViewById(R.id.logout_button);
        logout.setOnClickListener(v->{
            //launch logout activity
            Intent intent = new Intent(UserProfileActivity.this, LogoutActivity.class);
            startActivity(intent);
            finish();
        });
        Button home = findViewById(R.id.home_button);
        home.setOnClickListener(v-> {
            //launch home activity
            Intent intent = new Intent(UserProfileActivity.this, Home.class);
            startActivity(intent);
            finish();
        });
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        FirestoreHelper.loadUserMoods(username, dataList, moodArrayAdapter, this);
//    }
}

