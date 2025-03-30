package com.example.baobook.controller;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.baobook.constant.FirestoreConstants;

import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for Firestore operations.
 */
public class FirestoreHelper {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * function that loads either following or followers list from firestore
     * @param username username of current user
     * @param subCol subcollection to load (either followers or following or requests)
     * @param onSuccess callback for successful load
     * @param onFailure callback for failed load
     */
    public static void loadFollow(String username, String subCol,
                                     OnSuccessListener<List<User>> onSuccess,
                                     OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_USERS) // Navigate to Users collection
                .document(username) // Get the specific user
                .collection(subCol) // Get the Followers/Following collection
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FollowersActivity", "User has no followers.");
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }
                    ArrayList<User> userList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("FirestoreHelper", "Found user: " + document.getId());
                        String followUsername = document.getId(); // Each doc ID is a username
                        db.collection(FirestoreConstants.COLLECTION_USERS).document(followUsername).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        User user = userDoc.toObject(User.class);
                                        userList.add(user);
                                    }
                                    if (userList.size() == queryDocumentSnapshots.size()) {
                                        onSuccess.onSuccess(userList); // Return full list once all users are fetched
                                    }
                                })
                                .addOnFailureListener(e->{
                                    Log.e("FirestoreActivity", "Error fetching user", e);
                                    onSuccess.onSuccess(userList);
                                });
                        }
                    })
                    .addOnFailureListener(
                            e -> {
                                Log.e("FollowersActivity", "Error fetching followers", e);
                                onFailure.onFailure(e);
                            }
                    );
    }

    /**
     * search for users in firestore when search bar is used
     * @param query query to search for user
     * @param listener callback for result
     */
    public static void searchUsers(String query, OnUsersFetchedListener listener){
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .orderBy(FirestoreConstants.FIELD_USERNAME)
                .startAt(query)
                .endAt(query+"\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    //loop through results and add them to users list
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    listener.onUsersFound(users); //pass to callback
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error searching users", e));
    }
    public interface OnUsersFetchedListener {
        void onUsersFound(List<User> users);
    }

    /** This might not be being used
     * loads a users moods from firestore
     * @param dataList list to add moods to
     * @param adapter adapter to update
     * @param context context to use
     */
//    public static void loadUserMoods(List<MoodEvent> dataList, MoodEventArrayAdapter adapter, Context context) {
//        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        String username = prefs.getString("Username", null);
//        db = FirebaseFirestore.getInstance();
//        db.collection(FirestoreConstants.COLLECTION_USERS).document(username).collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    dataList.clear(); // Prevent duplicates
//                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
//                        MoodEvent mood = document.toObject(MoodEvent.class);
//                        if (mood != null) {
//                            dataList.add(mood);
//                        }
//                    }
//                    adapter.notifyDataSetChanged(); // Refresh UI
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Failed to load moods", Toast.LENGTH_SHORT).show();
//                });
//    }

    /**
     * adds a mood to firestore
     * @param mood mood to add
     * @param context context to use
     */
    public static void firestoreMood(MoodEvent mood, Context context) {
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("Username", null);
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)
                .collection("MoodEvents")
                .add(mood)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Mood added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add mood", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * sends a follow request from user to other user
     * @param user user from user session
     * @param otherUser user receiving request
     * @param context context to use
     */
    public static void requestFollow(User user, User otherUser, Context context){
        String username = user.getUsername();
        String otherUsername = otherUser.getUsername();
        if (username == null || otherUser.getUsername() == null) {
            Log.e("FirestoreHelper", "Error: username or otherUser is null.");
            Toast.makeText(context, "Error: Invalid user data.", Toast.LENGTH_SHORT).show();
            return;
        }
        db = FirebaseFirestore.getInstance();
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(otherUsername)
                .collection(FirestoreConstants.COLLECTION_REQUESTS)
                .document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(!documentSnapshot.exists()){
                        documentSnapshot.getReference().set(user);
                        Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show();
                        Log.d("FirestoreHelper", "Request sent to " + otherUsername);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * unfollows a user
     * @param user user that is unfollowing
     * @param otherUser user that is being unfollowed
     * @param context context to use
     */
    public static void unfollow(User user, User otherUser, Context context) {
        String username = user.getUsername();
        String otherUsername = otherUser.getUsername();
        if (username == null || otherUsername == null) {
            Log.e("FirestoreHelper", "Error: username or otherUser is null.");
            Toast.makeText(context, "Error: Invalid user data.", Toast.LENGTH_SHORT).show();
            return;
        }
        db = FirebaseFirestore.getInstance();
        //remove otherUser from users following list
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .document(otherUsername)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreHelper", username + " unfollowed " + otherUsername);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error unfollowing user", e);
                    Toast.makeText(context, "Failed to unfollow user", Toast.LENGTH_SHORT).show();
                });
        //remove current user from other user's followers list
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(otherUsername)
                .collection(FirestoreConstants.COLLECTION_FOLLOWERS)
                .document(username)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreHelper", username + " removed from " + otherUsername+" followers list");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error unfollowing user", e);
                    Toast.makeText(context, "Failed to unfollow user", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * follows a user. Called when accept button is clicked
     * @param currentUser user that is following
     * @param otherUser user that is being followed
     * @param context context to use
     */
    public static void followUser(User currentUser, User otherUser, Context context) {
        Log.d("FirestoreHelper", "followUser called. "+ currentUser.getUsername() + " is following " + otherUser.getUsername());
        String username = currentUser.getUsername();
        String otherUsername = otherUser.getUsername();
        if (username == null || otherUsername == null) {
            Log.e("FirestoreHelper", "Error: username or otherUser is null.");
            Toast.makeText(context, "Error: Invalid user data.", Toast.LENGTH_SHORT).show();
            return;
        }

        db = FirebaseFirestore.getInstance();
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)//go to current users document
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS) //add other user to following list
                .document(otherUsername)
                .set(otherUser)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreHelper", otherUsername + " added to " + username+"'s Following Collection");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to follow user", Toast.LENGTH_SHORT).show();
                });
        // Add current user to other user's followers list
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(otherUsername)
                .collection(FirestoreConstants.COLLECTION_FOLLOWERS)
                .document(username)
                .set(currentUser)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreHelper", username + " added to " + otherUsername+"'s Followers Collection");
                    })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to follow user", Toast.LENGTH_SHORT).show();
                });
        // Remove request from current user's requests list
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(otherUsername)
                .collection(FirestoreConstants.COLLECTION_REQUESTS)
                .document(username)
                .delete()
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreHelper", "Request removed from " + otherUsername + "'s requests list");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error removing request", e);
                    Toast.makeText(context, "Failed to follow user", Toast.LENGTH_SHORT).show();
                });
        }

    public static void updateUserExpAndLevel(String username, int expGain, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        onFailure.onFailure(new Exception("User not found"));
                        return;
                    }

                    int currentExp = documentSnapshot.getLong("exp").intValue();
                    int expNeeded = documentSnapshot.getLong("expNeeded").intValue();
                    int level = documentSnapshot.getLong("level").intValue();

                    currentExp += expGain;

                    while (currentExp >= expNeeded) {
                        currentExp = 0;
                        level += 1;
                        expNeeded += expNeeded * 2;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("exp", currentExp);
                    updates.put("level", level);
                    updates.put("expNeeded", expNeeded);

                    db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                            .update(updates)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);

                })
                .addOnFailureListener(onFailure);
    }


}
