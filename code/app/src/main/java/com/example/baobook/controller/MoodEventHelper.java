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
//    public void getMoodEventsByFollowing(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
//        db.collection(FirestoreConstants.COLLECTION_USERS)
//                .document(username)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        List<String> followingList = (List<String>) documentSnapshot.get(FirestoreConstants.FIELD_FOLLOWINGS);
//
//                        // If the following list is empty, return an empty list.
//                        if (followingList == null || followingList.isEmpty()) {
//                            onSuccess.onSuccess(new ArrayList<>());
//                            return;
//                        }
//
//                        // Get mood events from all followed users.
//                        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
//                                .whereIn(FirestoreConstants.FIELD_USERNAME, followingList)
//                                 .orderBy("timestamp", Query.Direction.DESCENDING) // Order by newest date first.
//                                .get()
//                                .addOnSuccessListener(querySnapshot -> {
//                                    List<MoodEvent> moodEvents = new ArrayList<>();
//                                    for (QueryDocumentSnapshot doc : querySnapshot) {
//                                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);
//                                        moodEvents.add(moodEvent);
//                                    }
//                                    onSuccess.onSuccess(moodEvents);
//                                })
//                                .addOnFailureListener(onFailure);
//                    } else {
//                        onFailure.onFailure(new RuntimeException("User not found: " + username));
//                    }
//                })
//                .addOnFailureListener(onFailure);
//    }

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
                            .limit(3)
                            .get()
                            .addOnSuccessListener(moodSnapshot -> {
                                Log.d("MoodEventHelper", "Got mood events. Size: " + moodSnapshot.size());
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : moodSnapshot) {
                                    MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                                    Log.d("MoodEventHelper", "Found mood event from: " + moodEvent.getUsername());
                                    moodEvents.add(moodEvent);
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
