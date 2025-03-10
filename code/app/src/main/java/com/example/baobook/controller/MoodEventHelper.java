package com.example.baobook.controller;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.model.MoodEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles getting Users' own MoodEvents as well as following MoodEvents.
 */
public class MoodEventHelper {
    private final FirebaseFirestore db;

    public MoodEventHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Gets all MoodEvents authored by a given user.
     * MoodEvents are returned in chronological order, newest first.
     * @param username  The username of the user to MoodEvents from.
     * @param onSuccess  Callback triggered upon successful retrieval.
     * @param onFailure  Callback triggered on failure.
     */
    public void getMoodEventsByUser(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .whereEqualTo(FirestoreConstants.FIELD_USERNAME, username)
                // Todo: Currently, we store date and time in separate fields.
                //  Store this in one field so that we can easily sort MoodEvents by "timestamp" (date and time).
                .orderBy("date", Query.Direction.DESCENDING) // Order by newest date first
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<MoodEvent> moodEvents = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                        moodEvents.add(moodEvent);
                    }
                    onSuccess.onSuccess(moodEvents);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Gets all MoodEvents from a given user's following list.
     * MoodEvents are returned in chronological order, newest first.
     * @param username  The username of the user to get following MoodEvents from.
     * @param onSuccess  Callback triggered upon successful retrieval.
     * @param onFailure  Callback triggered on failure.
     */
    public void getMoodEventsByFollowing(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> followingList = (List<String>) documentSnapshot.get(FirestoreConstants.FIELD_FOLLOWINGS);

                        // If the following list is empty, return an empty list.
                        if (followingList == null || followingList.isEmpty()) {
                            onSuccess.onSuccess(new ArrayList<>());
                            return;
                        }

                        // Get mood events from all followed users.
                        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                                .whereIn(FirestoreConstants.FIELD_USERNAME, followingList)
                                 .orderBy("date", Query.Direction.DESCENDING) // Order by newest date first.
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<MoodEvent> moodEvents = new ArrayList<>();
                                    for (QueryDocumentSnapshot doc : querySnapshot) {
                                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                                        moodEvents.add(moodEvent);
                                    }
                                    onSuccess.onSuccess(moodEvents);
                                })
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new RuntimeException("User not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }
}
