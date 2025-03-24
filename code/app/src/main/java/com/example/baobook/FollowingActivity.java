package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.model.User;
import com.example.baobook.util.UserSession;

import java.util.ArrayList;

/**
 * Activity to display a list of users that the current user is following. User can
 * select a another user in the list and be led to their profile
 */

public class FollowingActivity extends AppCompatActivity {
    private UserSession userSession;
    private ArrayList<User> followingList;
    private UserArrayAdapter adapter;
    private ListView userList;
    Button backButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        userSession = new UserSession(this);
        followingList = new ArrayList<>();
        adapter = new UserArrayAdapter(this, followingList, "FollowingActivity", null);
        userList = findViewById(R.id.following_list);
        backButton = findViewById(R.id.back_button);
        userList.setAdapter(adapter);

        loadFollowing();

        userList.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = followingList.get(position);
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userID", selectedUser.getUsername());
            startActivity(intent);
        });
        backButton.setOnClickListener(v -> { finish();});
    }
    /*
    function to load followers from firestore and populate following list
    Calls loadFollowing from FirestoreHelper class to fetch following users in database
     */
    private void loadFollowing() {
        FirestoreHelper.loadFollow(userSession.getUsername(), FirestoreConstants.COLLECTION_FOLLOWINGS, users -> {
            Log.e("FirestoreDebug", "loadFollow called for user: ");
            if (users != null) {
                if(users.isEmpty()){
                    Log.e("FollowingActivity", "User has no followers.");
                }
                Log.e("FollowingActivity", "Found " + users.size() + " following");
                followingList.clear();
                followingList.addAll(users);
                adapter.notifyDataSetChanged();
            }else{
                Log.e("FollowingActivity", "users is null.");
            }
        }, e -> Log.e("FollowingActivity", "Error fetching following list", e));
    }
}
