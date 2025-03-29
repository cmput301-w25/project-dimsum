package com.example.baobook.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.baobook.constant.SharedPreferencesConstants;
import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.exception.AuthenticationException;
import com.example.baobook.exception.UserNotFoundException;
import com.example.baobook.exception.UsernameAlreadyExistsException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication and session management.
 */
public class AuthHelper {
    private final FirebaseFirestore db;
    private final SharedPreferences sharedPreferences;

    public AuthHelper(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.USER_SESSION, Context.MODE_PRIVATE);
    }


    /**
     * Checks if a user with a given username already exists
     *
     * @param username  The username.
     * @param onSuccess  Callback triggered when the check is successful.
     * @param onFailure  Callback triggered when the check fails.
     */
    public void userExists(String username, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(FirestoreConstants.COLLECTION_USERS) // Change to your actual collection name
                .document(username) // Assuming username is used as the document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean exists = documentSnapshot.exists();
                    onSuccess.onSuccess(exists);
                })
                .addOnFailureListener(onFailure);
    }


    /**
     * Registers a user.
     *
     * @param username  The unique username.
     * @param password  The password.
     * @param onSuccess  Callback triggered when registration is successful.
     * @param onFailure  Callback triggered when registration fails.
     */
    public void registerUser(String username, String password, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        onFailure.onFailure(new UsernameAlreadyExistsException(username));
                        return;
                    }

                    // Add the user to Firestore
                    Map<String, Object> userData = new HashMap<>();
                    userData.put(FirestoreConstants.FIELD_USERNAME, username);
                    userData.put(FirestoreConstants.FIELD_PASSWORD, password);
                    userData.put("level", 0);
                    userData.put("exp", 0);
                    userData.put("expNeeded", 10);

                    db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> {
                                // Save session in SharedPreferences only if registration succeeds
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(SharedPreferencesConstants.USERNAME, username);
                                editor.putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, true);
                                editor.apply();

                                onSuccess.onSuccess(null);
                            })
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }


    /**
     * Log in to an existing login.
     *
     * @param username  The username.
     * @param password  The password.
     * @param onSuccess  Callback triggered when login is successful.
     * @param onFailure  Callback triggered when login fails.
     */
    public void loginUser(String username, String password, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        onFailure.onFailure(new UserNotFoundException(username));
                        return;
                    }

                    String storedPassword = documentSnapshot.getString(FirestoreConstants.FIELD_PASSWORD);
                    if (storedPassword == null) {
                        onFailure.onFailure(new AuthenticationException("No password found for user: " + username));
                        return;
                    }
                    if (!password.equals(storedPassword)) {
                        onFailure.onFailure(new AuthenticationException("Incorrect password for user: " + username));
                        return;
                    }

                    // The passwords match. Update SharedPreferences.
                    loginSharedPreferences(username);
                    Map<String, Object> updates = new HashMap<>();

                    Long levelRaw = documentSnapshot.getLong("level");
                    Long expRaw = documentSnapshot.getLong("exp");
                    Long expNeededRaw = documentSnapshot.getLong("expNeeded");

                    int levelVal = 0;
                    int expVal = 0;
                    int expNeededVal = 10;

                    if (levelRaw != null) {
                        levelVal = levelRaw.intValue();
                    } else {
                        updates.put("level", levelVal);
                    }

                    if (expRaw != null) {
                        expVal = expRaw.intValue();
                    } else {
                        updates.put("exp", expVal);
                    }

                    if (expNeededRaw != null) {
                        expNeededVal = expNeededRaw.intValue();
                    } else {
                        updates.put("expNeeded", expNeededVal);
                    }

                    // Save synced values to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("level", levelVal);
                    editor.putInt("exp", expVal);
                    editor.putInt("expNeeded", expNeededVal);
                    editor.apply();

                    // If there were any missing fields, write them to Firestore
                    if (!updates.isEmpty()) {
                        db.collection(FirestoreConstants.COLLECTION_USERS)
                                .document(username)
                                .update(updates);
                    }
                    onSuccess.onSuccess(null);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Ends a user session. Updates SharedPreferences.UserSession accordingly.
     */
    public void logoutUser() {
        // Update SharedPreferences to end the user session.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.USERNAME, null);
        editor.putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false);
        editor.apply();
    }

    /**
     * Begins a user session. Updates SharedPreferences.UserSession accordingly.
     * @param username The username of the user to begin the session
     */
    private void loginSharedPreferences(String username) {
        // Updates SharedPreferences to reflect the logged-in user session.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.USERNAME, username);
        editor.putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, true);
        editor.apply();
    }
}

