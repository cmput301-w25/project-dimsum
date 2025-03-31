package com.example.baobook;

import android.content.Context;
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
 * Activity to display a list of followers of the current user.
 */
public class FollowersActivity extends AppCompatActivity {
    private UserSession userSession;
    private ArrayList<User> followerList;
    private UserArrayAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        userSession = new UserSession(this);
        followerList = new ArrayList<>();
        adapter = new UserArrayAdapter((Context) this, followerList, "FollowersActivity", null);
        ListView userList = findViewById(R.id.followers_list);
        Button backButton = findViewById(R.id.back_button);
        userList.setAdapter(adapter);

        loadFollowers();

        userList.setOnItemClickListener((parent, view, position, id) -> {
            User user = followerList.get(position);
            //need to implement a way to see someone elses profile
            Intent intent = new Intent(FollowersActivity.this, UserProfileActivity.class);
            intent.putExtra("userID", user.getUsername());
            startActivity(intent);
        });
        backButton.setOnClickListener(v -> {finish();});
    }

    /*
    function to load followers from firestore and populate followers list
    Calls loadFollowers from FirestoreHelper class to fetch followers in database
    */
    private void loadFollowers() {
        FirestoreHelper.loadFollow(userSession.getUsername(), FirestoreConstants.COLLECTION_FOLLOWERS, users -> {
            if (users != null) {
                followerList.clear();
                followerList.addAll(users);
                adapter.notifyDataSetChanged();
                Log.d("FollowersActivity", "Found " + users.size() + " followers");
            } else {
                Log.e("FollowersActivity", "User has no followers.");
            }
        }, e -> Log.e("FollowersActivity", "Error fetching following list", e));
    }
}
