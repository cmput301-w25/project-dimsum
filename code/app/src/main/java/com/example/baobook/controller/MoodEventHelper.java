package com.example.baobook.controller;

import android.util.Log;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.model.Comment;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.Privacy;
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
     * Gets the 3 most recent mood events from users that the given user is following.
     * MoodEvents are returned in reverse chronological order (newest first).
     * @param username  The username of the user to get following MoodEvents from.
     * @param onSuccess  Callback triggered upon successful retrieval.
     * @param onFailure  Callback triggered on failure.
     */
    public void getRecentFollowingMoodEvents(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        Log.d("MoodEventHelper", "Getting recent mood events for user: " + username);

        // First get the list of users being followed from the followings subcollection
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("MoodEventHelper", "Got followings collection. Size: " + querySnapshot.size());

                    if (querySnapshot.isEmpty()) {
                        Log.d("MoodEventHelper", "No users being followed");
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Extract usernames from the following documents
                    List<String> followingUsernames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String followedUsername = doc.getId();
                        Log.d("MoodEventHelper", "Found followed user: " + followedUsername);
                        followingUsernames.add(followedUsername);
                    }

                    Log.d("MoodEventHelper", "Querying mood events for " + followingUsernames.size() + " followed users");

                    // Get mood events from all followed users, limit to 3
                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                            .whereIn(FirestoreConstants.FIELD_USERNAME, followingUsernames)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(15) //increased limit to 15 to get more mood events to filter through
                            .get()
                            .addOnSuccessListener(moodSnapshot -> {
                                Log.d("MoodEventHelper", "Got mood events. Size: " + moodSnapshot.size());
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : moodSnapshot) {
                                    MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                                    if(moodEvents.size()<3){
                                        if(moodEvent.getPrivacy()== Privacy.PUBLIC){
                                            Log.d("MoodEventHelper", "Found public mood event from: " + moodEvent.getUsername());
                                            moodEvents.add(moodEvent);
                                        }
                                        Log.d("MoodEventHelper","Found private mood event from: " + moodEvent.getUsername());
                                    }
                                }
                                onSuccess.onSuccess(moodEvents);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MoodEventHelper", "Error getting mood events", e);
                                onFailure.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodEventHelper", "Error getting followings collection", e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Retrieves all public mood events from users that the specified user follows.
     * @param username  The username of the user to get following MoodEvents from.
     * @param onSuccess  Callback triggered upon successful retrieval.
     * @param onFailure  Callback triggered on failure.
     */
    public void getAllFollowingMoodEvents(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        Log.d("MoodEventHelper", "Getting ALL following mood events for user: " + username);

        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("MoodEventHelper", "Got followings collection. Size: " + querySnapshot.size());

                    if (querySnapshot.isEmpty()) {
                        Log.d("MoodEventHelper", "No users being followed");
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> followingUsernames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String followedUsername = doc.getId();
                        Log.d("MoodEventHelper", "Following: " + followedUsername);
                        followingUsernames.add(followedUsername);
                    }

                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                            .whereIn(FirestoreConstants.FIELD_USERNAME, followingUsernames)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(moodSnapshot -> {
                                Log.d("MoodEventHelper", "Got ALL mood events from followed users. Size: " + moodSnapshot.size());
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : moodSnapshot) {
                                    MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                                    if (moodEvent.getPrivacy() == Privacy.PUBLIC) {
                                        Log.d("MoodEventHelper", "Adding public mood event from: " + moodEvent.getUsername());
                                        moodEvents.add(moodEvent);
                                    } else {
                                        Log.d("MoodEventHelper", "Skipping private mood event from: " + moodEvent.getUsername());
                                    }
                                }
                                onSuccess.onSuccess(moodEvents);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MoodEventHelper", "Failed to get mood events from followed users", e);
                                onFailure.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodEventHelper", "Failed to get followings list", e);
                    onFailure.onFailure(e);
                });
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
        if (moodEventId != null) {
            db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                    .document(moodEventId)
                    .collection(FirestoreConstants.COLLECTION_COMMENTS)
                    .add(comment)
                    .addOnSuccessListener(aVoid -> {
                        if (onSuccess != null) {
                            onSuccess.onSuccess(null);
                        }
                        Log.d("MoodEventHelper", "Comment added successfully");
                    })
                    .addOnFailureListener(e -> {
                        if (onFailure != null) {
                            onFailure.onFailure(e);
                        }
                    });
        }
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
