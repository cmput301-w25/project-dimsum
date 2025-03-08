package com.example.baobook.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.baobook.constant.SharedPreferencesConstants;
import com.example.baobook.constant.FirestoreConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthHelper {
    private final FirebaseFirestore db;
    private final SharedPreferences sharedPreferences;

    public AuthHelper(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.USER_SESSION, Context.MODE_PRIVATE);
    }

    /**
     * Registers a new user in Firestore.
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
                        // Username validation should occur before attempting to register the user.
                        onFailure.onFailure(new RuntimeException("Attempted to register a user with a taken username"));
                    } else {
                        // Add the user to Firestore.
                        Map<String, Object> userData = new HashMap<>();
                        userData.put(FirestoreConstants.FIELD_USERNAME, username);
                        userData.put(FirestoreConstants.FIELD_PASSWORD, password);

                        db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                                .set(userData)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);

                        // Update "UserSession" in SharedPreferences, adding the username and logged-in state.
                        // This allows the user to remain authenticated after closing/opening the app.
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(SharedPreferencesConstants.USERNAME, username);
                        editor.putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, true);
                        editor.apply();
                    }
                })
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Error registering user: " + e.getMessage(), e.getCause());
                });
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
                    if (documentSnapshot.exists()) {
                        Object storedPassword = documentSnapshot.get(FirestoreConstants.FIELD_PASSWORD);

                        if (storedPassword == null) {
                            onFailure.onFailure(new Exception(String.format("No password found for user: %s", username)));
                            return;
                        }
                        if (!password.equals(storedPassword)) {
                            onFailure.onFailure(new Exception(String.format("Incorrect password for user: %s", username)));
                            return;
                        }

                        // The passwords match. Update SharedPreferences.
                        loginSharedPreferences(username);
                        // Notify success.
                        onSuccess.onSuccess(null);
                    } else {
                        // The user doesn't exist.
                        onFailure.onFailure(new Exception(String.format("Username not found: %s", username)));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void logoutUser() {
        // Update SharedPreferences to end the user session.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.USERNAME, null);
        editor.putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false);
        editor.apply();
    }

    private void loginSharedPreferences(String username) {
        // Updates SharedPreferences to reflect the logged-in user session.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesConstants.USERNAME, username);
        editor.putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, true);
        editor.apply();
    }
}

