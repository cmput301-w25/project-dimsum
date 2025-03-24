package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.controller.UserHelper;
import com.example.baobook.model.User;
import com.example.baobook.util.UserSession;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private ListView userList;
    private User selectedUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_requests_activity);
        userSession = new UserSession(this);
        requestedList = new ArrayList<>();
        adapter = new UserArrayAdapter(this, requestedList, "FollowRequestsActivity", this);
        userList = findViewById(R.id.requests_list);
        userList.setAdapter(adapter);

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
            Log.e("FirestoreDebug", "loadFollow called for user: ");
            if (users != null) {
                if(users.isEmpty()){
                    Log.e("FollowRequestsActivity", "User has no requests.");
                }
                Log.e("FollowRequestsActivity", "Found " + users.size() + " requests");
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
