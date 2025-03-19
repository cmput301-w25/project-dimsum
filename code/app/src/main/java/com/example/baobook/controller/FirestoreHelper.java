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
import java.util.List;

/**
 * Helper class for Firestore operations.
 */
public class FirestoreHelper {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * Callback interface for user addition result.
     */
    public interface UserCallback {
        /**
         * Called when the addition is complete.
         * @param success true if addition was successful, false otherwise
         */
        void onResult(boolean success);
    }

    /**
     * Callback interface for username/password check result.
     */
    public interface UsernamePasswordCallback {
        /**
         * Called when the check is complete.
         * @param success true if username and password match, false otherwise
         */
        void onResult(boolean success);
    }

    /**
     * Callback interface for username existence check result.
     */
    public interface UsernameExistsCallback {
        /**
         * Called when the check is complete.
         * @param exists true if username already exists, false otherwise
         */
        void onResult(boolean exists);
    }


    public interface FollowCallback {
        void onCallback(ArrayList<User> follow);
    }

    /**
     * Check if username and password match an existing user (for login)
     * @param username username to check
     * @param password password to check
     * @param callback callback for result
     */
    public static void checkUsernamePassword(String username, String password, FirestoreHelper.UsernamePasswordCallback callback) {
        db.collection("Users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean success = !task.getResult().isEmpty(); // True if username & password match
                        callback.onResult(success);
                    } else {
                        // Handle firestore error
                        Log.e("FirestoreError", "Error checking username/password", task.getException());
                        callback.onResult(false);
                    }
                });
    }
    /**
     * Check if a username already exists (for signup validation)
     * @param username username to check
     * @param callback callback for result
     */
    public static void checkIfUsernameExists(String username, UsernameExistsCallback callback) {
        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean exists = !task.getResult().isEmpty();
                        callback.onResult(exists);
                    } else {
                        Log.e("FirestoreError", "Error checking username existence", task.getException());
                        callback.onResult(false); // Assume it doesn't exist if query fails
                    }
                });
    }

    /**
     * Add a new user to the database
     * @param user user to add
     * @param callback callback for result
     */
    public static void addUser(User user, UserCallback callback) {
        db.collection("Users")
                .document(user.getUsername()) // Use username as document ID
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(true);
                    } else {
                        Log.e("FirestoreError", "Error adding user", task.getException());
                        callback.onResult(false);
                    }
                });
    }

    /**
     * function that loads either following or followers list from firestore
     * @param username username of current user
     * @param subCol subcollection to load (either followers or following)
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
                        Log.e("FollowersActivity", "User has no followers.");
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }
                    ArrayList<User> userList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Log.e("FirestoreHelper", "Found user: " + document.getId());
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
    public void searchUsers(String query, OnUsersFetchedListener listener){
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
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * follows a user. Called when accept button is clicked
     * @param currentUser user that is following
     * @param otherUser user that is being followed
     * @param context context to use
     */
    public static void followUser(User currentUser, User otherUser, Context context) {
        String username = currentUser.getUsername();
        String otherUsername = otherUser.getUsername();
        if (username == null || otherUsername == null) {
            Log.e("FirestoreHelper", "Error: username or otherUser is null.");
            Toast.makeText(context, "Error: Invalid user data.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Add other user to current user's following list
        db = FirebaseFirestore.getInstance();
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)//go to current users document
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS) //add other user to following list
                .document(otherUsername)
                .set(otherUser)
                .addOnSuccessListener(documentReference -> {
                    Log.e("FirestoreHelper", username + " followed " + otherUsername);
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
                    Log.e("FirestoreHelper", username + " followed " + otherUsername);
                    })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to follow user", Toast.LENGTH_SHORT).show();
                });
        // Remove request from current user's requests list
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)
                .collection(FirestoreConstants.COLLECTION_REQUESTS)
                .document(otherUsername)
                .delete();
    }

    /**
     * loads follow requests from firestore. Uses load follow to load requests in the requests collection
     * @param username username of current user
     * @param onSuccess callback for successful load
     * @param onFailure callback for failed load
     */
    public static void loadFollowRequests(String username, OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure){
        loadFollow(username, FirestoreConstants.COLLECTION_REQUESTS, onSuccess, onFailure);
    }
}