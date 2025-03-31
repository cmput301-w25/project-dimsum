package com.example.baobook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.adapter.UserArrayAdapter;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        userSession = new UserSession(this);
        followingList = new ArrayList<>();
        adapter = new UserArrayAdapter(this, followingList, "FollowingActivity", null);
        ListView userList = findViewById(R.id.following_list);
        Button backButton = findViewById(R.id.back_button);
        userList.setAdapter(adapter);

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
            if (users != null) {
                followingList.clear();
                followingList.addAll(users);
                adapter.notifyDataSetChanged();
                Log.d("FollowingActivity", "Found " + users.size() + " following");
            }else{
                Log.e("FollowingActivity", "users is null.");
            }
        }, e -> Log.e("FollowingActivity", "Error fetching following list", e));
    }
}
