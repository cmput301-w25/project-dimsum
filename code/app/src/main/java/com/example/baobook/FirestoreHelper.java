package com.example.baobook;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Firestore operations.
 */
public class FirestoreHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback interface for user addition result.
     */
    public interface UserCallback {
        /**
         * Called when the addition is complete.
         *
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
         *
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
         *
         * @param exists true if username already exists, false otherwise
         */
        void onResult(boolean exists);
    }

    // Define a callback interface
    public interface FollowCallback {
        void onCallback(ArrayList<User> follow);
    }

    // Check if username and password match an existing user (for login)
    public static void checkUsernamePassword(String username, String password, FirestoreHelper.UsernamePasswordCallback callback) {
        db.collection("users")
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

    // Check if a username already exists (for signup validation)
    public static void checkIfUsernameExists(String username, UsernameExistsCallback callback) {
        db.collection("users")
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

    // Add a new user to Firestore
    public static void addUser(User user, UserCallback callback) {
        db.collection("users")
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
     *
     * @param username username of current user
     * @param callback callback to return followers
     * @param isFollowing true if getting followers, false if getting following
     */
    public static void loadFollow(String username, FollowCallback callback, boolean isFollowing) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(username)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> followUsernames;
                        if(isFollowing){
                            followUsernames = (List<String>) document.get("followers");
                        }else{
                            followUsernames = (List<String>) document.get("following");
                        }

                        if (followUsernames == null || followUsernames.isEmpty()) {
                            callback.onCallback(new ArrayList<>());  // Return empty list if null
                            return;
                        }
                        ArrayList<User> followers = new ArrayList<>();
                        for (String fu : followUsernames) {
                            db.collection("users").document(username).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            User user = userDoc.toObject(User.class);
                                            followers.add(user);
                                        }
                                        if (followers.size() == followUsernames.size()) {
                                            callback.onCallback(followers);
                                        }
                                    });
                        }
                    } else {
                        callback.onCallback(new ArrayList<>());  // Return empty list if user not found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting followers", e);
                    callback.onCallback(null);  // Return null if an error occurs
                });
    }
    public static void loadUserMoods(String username, List<MoodEvent> dataList, MoodEventArrayAdapter adapter, Context context) {
        CollectionReference moodsRef = db.collection("MoodEvents");
        moodsRef.whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        MoodEvent mood = document.toObject(MoodEvent.class);
                        dataList.add(mood);
                    }
                    adapter.notifyDataSetChanged(); //refresh UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load moods", Toast.LENGTH_SHORT).show();
                });
    }
}