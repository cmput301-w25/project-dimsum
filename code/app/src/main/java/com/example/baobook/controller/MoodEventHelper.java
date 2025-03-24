package com.example.baobook.controller;


import android.util.Log;


import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.Comment;
import com.example.baobook.model.Privacy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
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
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by newest date first
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
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS) // Access the subcollection
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> followingList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String followedUser = doc.getId(); // Assuming the document ID is the username of the followed user
                        Log.d("MoodEventHelper", "Found followed user: " + followedUser);
                        followingList.add(followedUser);
                    }

                    if (followingList.isEmpty()) {
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Query mood events using whereIn for the followed users
                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                            .whereIn(FirestoreConstants.FIELD_USERNAME, followingList)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(eventsSnapshot -> {
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : eventsSnapshot) {
                                    MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                                    if(moodEvent.getPrivacy() == Privacy.PUBLIC){ //only add PUBLIC mood events
                                        Log.d("MoodEventHelper", "Found mood event: " + moodEvent);
                                        moodEvents.add(moodEvent);
                                    }
                                }
                                onSuccess.onSuccess(moodEvents);
                            })
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }
    /**
     * Publishes a new MoodEvent to Firestore.
     * Checks if the MoodEvent with the given ID already exists.
     * If it exists, the operation fails. Otherwise, the MoodEvent is added to Firestore.
     *
     * @param moodEvent The {@link MoodEvent} to be published.
     * @param onSuccess Callback triggered when the MoodEvent is successfully published.
     * @param onFailure Callback triggered when publishing fails (e.g., MoodEvent already exists).
     */
    public void publishMood(MoodEvent moodEvent, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String id = moodEvent.getId();
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        onFailure.onFailure(new RuntimeException("Mood event already exists: " + documentSnapshot.toObject(MoodEvent.class)));
                        return;
                    }

                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(id)
                            .set(moodEvent)
                            .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Updates an existing MoodEvent in Firestore.
     * Checks if the MoodEvent exists in Firestore. If it does not exist,
     * the operation fails. Otherwise, the existing MoodEvent is updated with new data.
     *
     * @param moodEvent The {@link MoodEvent} containing updated data.
     * @param onSuccess Callback triggered when the MoodEvent is successfully updated.
     * @param onFailure Callback triggered when updating fails (e.g., MoodEvent does not exist).
     */
    public void updateMood(MoodEvent moodEvent, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String id = moodEvent.getId();
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        onFailure.onFailure(new RuntimeException("Mood event doesn't exist."));
                        return;
                    }

                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(id)
                            .set(moodEvent)
                            .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Updates an existing MoodEvent in Firestore.
     * Checks if the MoodEvent exists in Firestore. If it does not exist,
     * the operation fails. Otherwise, the existing MoodEvent is updated with new data.
     *
     * @param moodEvent The {@link MoodEvent} containing updated data.
     * @param onSuccess Callback triggered when the MoodEvent is successfully updated.
     * @param onFailure Callback triggered when updating fails (e.g., MoodEvent does not exist).
     */
    public void deleteMood(MoodEvent moodEvent, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String id = moodEvent.getId();
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(id).delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Adds a comment to a mood event. Comments are stored as a subcollection for each moodEvent
     * @param moodEventId The ID of the mood event to add the comment to.
     * @param comment The comment object to add.
     * @param onSuccess Callback triggered upon successful addition.
     * @param onFailure Callback triggered on failure.
     */
    public void addComment(String moodEventId, Comment comment, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document(moodEventId)
                .collection(FirestoreConstants.COLLECTION_COMMENTS)
                .add(comment)
                .addOnSuccessListener(aVoid-> {
                    onSuccess.onSuccess(null); // Correct call
                    Log.d("MoodEventHelper", "Comment added successfully");
                })
                .addOnFailureListener(onFailure);
    }
    /**
     * Loads comments for a given mood event.
     * @param moodEventId The ID of the mood event to load comments for.
     * @param onSuccess Callback triggered upon successful retrieval.
     * @param onFailure Callback triggered on failure.
     */
    public void loadComments(String moodEventId, OnSuccessListener<List<Comment>> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document(moodEventId)
                .collection(FirestoreConstants.COLLECTION_COMMENTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Comment comment = doc.toObject(Comment.class);
                        comments.add(comment);
                    }
                    onSuccess.onSuccess(comments);
                })
                .addOnFailureListener(onFailure);
    }
}
