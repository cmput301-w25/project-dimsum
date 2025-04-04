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
 * Activity for handling follow requests. Shows a list of users that have sent a follow request to the current user.
 * When a user is clicked, they are taken to their profile. When the accept button is clicked, the follow request is accepted and the
 * follower is added to the current user's followers list. The request is removed from the list.
 */
public class FollowRequestsActivity extends AppCompatActivity implements UserArrayAdapter.OnAcceptListener {
    private UserSession userSession;
    private ArrayList<User> requestedList;
    private UserArrayAdapter adapter;
    private User selectedUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_requests_activity);
        userSession = new UserSession(this);
        requestedList = new ArrayList<>();
        adapter = new UserArrayAdapter(this, requestedList, "FollowRequestsActivity", this);
        ListView userList = findViewById(R.id.requests_list);
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

        loadFollowRequests();

        userList.setOnItemClickListener((parent, view, position, id) -> {
            selectedUser = requestedList.get(position);
            Intent intent = new Intent(FollowRequestsActivity.this, UserProfileActivity.class);
            intent.putExtra("username", selectedUser.getUsername());
            startActivity(intent);
        });
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {finish();});
    }

    /**
     * function to call firestore to load followers and populate followers list
     */
    private void loadFollowRequests() {
        FirestoreHelper.loadFollow(userSession.getUsername(), FirestoreConstants.COLLECTION_REQUESTS, users -> {
            Log.d("FirestoreDebug", "loadFollowRequests called for user: " + userSession.getUsername());
            if (users != null) {
                if(users.isEmpty()){
                    Log.d("FollowRequestsActivity", "User has no requests.");
                }
                Log.d("FollowRequestsActivity", "Found " + users.size() + " requests");
                requestedList.clear();
                requestedList.addAll(users);
                adapter.notifyDataSetChanged();
            }else{
                Log.e("FollowRequestsActivity", "users is null.");
            }
        }, e -> Log.e("FollowRequestsActivity", "Error fetching following list", e));
    }

    /**
     * function to handle accept button click
     * @param user the user to accept
     * @param position the position of the user in the list
     */
    @Override
    public void onAccept(User user, int position) {
        FirestoreHelper.followUser(user, userSession.getUser(), FollowRequestsActivity.this);
        adapter.removeUser(position);
        adapter.notifyDataSetChanged();
    }
}
