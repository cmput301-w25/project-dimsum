package com.example.baobook;

import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.controller.UserHelper;
import com.example.baobook.model.MoodEvent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.model.User;
import com.example.baobook.util.UserSession;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    TextView usernameText;
    private boolean isCurrentUser;
    private UserHelper userHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        userHelper = new UserHelper();
        UserSession session = new UserSession(this);
        username = session.getUsername();
        User user = session.getUser();
        usernameText = findViewById(R.id.username_text);

        String otherUsername = getIntent().getStringExtra("userID");
        isCurrentUser = (otherUsername==null||otherUsername.equalsIgnoreCase(username));

        usernameText.setText(isCurrentUser ? username : otherUsername);

        Button followersButton = findViewById(R.id.followers_button);
        Button followingButton = findViewById(R.id.following_button);
        Button followButton = findViewById(R.id.follow_button);
        Button requestsButton = findViewById(R.id.requests_button);

        if(isCurrentUser){
            followButton.setVisibility(Button.GONE);
            followingButton.setOnClickListener(v -> startActivity(new Intent(this, FollowingActivity.class)));
            followersButton.setOnClickListener(v -> startActivity(new Intent(this, FollowersActivity.class)));
            requestsButton.setOnClickListener(v -> startActivity(new Intent(this, FollowRequestsActivity.class)));
        }else{
            followButton.setVisibility(Button.VISIBLE);
            followersButton.setVisibility(Button.GONE);
            followingButton.setVisibility(Button.GONE);
            requestsButton.setVisibility(Button.GONE);
            User otherUser = new User();
            otherUser.setUsername(otherUsername);
            userHelper.checkFollowStatus(username, otherUsername,
                    status -> {
                        boolean isFollowing = status.first;
                        boolean hasRequested = status.second;

                        // Set button text based on follow status
                        if (isFollowing) {
                            followButton.setText("Unfollow");
                        } else if (hasRequested) {
                            followButton.setText("Requested");
                        } else {
                            followButton.setText("Follow");
                        }

                        // Set follow button action
                        followButton.setOnClickListener(v -> {
                            if (isFollowing) {
                                // Unfollow the user
//                                FirestoreHelper.unfollow(username, otherUsername, UserProfileActivity.this);
                                followButton.setText("Following");
                            } else {
                                // Send follow request
                                FirestoreHelper.requestFollow(user, otherUser, UserProfileActivity.this);
                                followButton.setText("Requested");
                            }
                        });
                    },
                    e -> Log.e("FollowCheck", "Error checking follow status: " + e.getMessage())
            );
            followButton.setOnClickListener(v -> {
                // Handle follow button click
                Toast.makeText(UserProfileActivity.this, "Follow request sent!", Toast.LENGTH_SHORT).show();
                FirestoreHelper.requestFollow(user, otherUser, UserProfileActivity.this);
            });
        }
        // Floating Action Button to add a new mood
        FloatingActionButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            // Launch AddMoodActivity
            Intent intent = new Intent(UserProfileActivity.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
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
            // Launch Home activity
            Intent intent = new Intent(UserProfileActivity.this, Home.class);
            startActivity(intent);
            finish();
        });
        Button maps = findViewById(R.id.map_button);
        maps.setOnClickListener(v-> {
            // Launch MapsActivity
            Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        });
        Button profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> {
            // Launch UserProfileActivity
            Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
        Button moodHistoryButton = findViewById(R.id.mood_history_button);
        moodHistoryButton.setOnClickListener(v -> {
            // Launch MoodHistory activity
            Intent intent = new Intent(UserProfileActivity.this, MoodHistory.class);
            startActivity(intent);
        });
    }
    @Override
    public void onEditMoodEvent(MoodEvent mood) {
        EditFragment fragment = new EditFragment(mood);
        fragment.show(getSupportFragmentManager(), "Edit Mood");
    }
    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
        for (int i = 0; i < dataList.size(); i++) {
            MoodEvent mood = dataList.get(i);
            if (mood.getId().equals(updatedMoodEvent.getId())) {
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
}

