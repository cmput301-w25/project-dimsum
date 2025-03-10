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
 * helps get mood events
 */
public class MoodEventHelper {
    private final FirebaseFirestore db;

    public MoodEventHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

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

    public void getMoodEventsByFollowing(String username, OnSuccessListener<List<MoodEvent>> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
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
                                // .orderBy("date", Query.Direction.DESCENDING) // Order by newest date first.
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


////
////    public void getFromUsersFollowing(String username, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
////        DocumentReference currentUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(currentUser);
////        DocumentReference targetUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser);
////
////        db.runTransaction(transaction -> {
////            DocumentSnapshot currentUserDoc = transaction.get(currentUserRef);
////            DocumentSnapshot targetUserDoc = transaction.get(targetUserRef);
//
//            if (!currentUserDoc.exists() || !targetUserDoc.exists()) {
//                throw new RuntimeException("One or both users do not exist.");
//            }
//
//            List<String> following = (List<String>) currentUserDoc.get("followings");
//            List<String> followers = (List<String>) targetUserDoc.get("followers");
//
//            if (following != null && !following.contains(targetUser)) {
//                following.add(targetUser);
//            }
//            if (followers != null && !followers.contains(currentUser)) {
//                followers.add(currentUser);
//            }
//
//            transaction.update(currentUserRef, "followings", following);
//            transaction.update(targetUserRef, "followers", followers);
//
//            return null;
//        }).addOnSuccessListener(aVoid -> {
//            onSuccess.onSuccess(null);
//        }).addOnFailureListener(onFailure);
//    }
//
//    public void unfollowUser(String currentUser, String targetUser, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
//        DocumentReference currentUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(currentUser);
//        DocumentReference targetUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser);
//
//        db.runTransaction(transaction -> {
//            DocumentSnapshot currentUserDoc = transaction.get(currentUserRef);
//            DocumentSnapshot targetUserDoc = transaction.get(targetUserRef);
//
//            if (!currentUserDoc.exists() || !targetUserDoc.exists()) {
//                throw new RuntimeException("One or both users do not exist.");
//            }
//
//            List<String> following = (List<String>) currentUserDoc.get("followings");
//            List<String> followers = (List<String>) targetUserDoc.get("followers");
//
//            if (following != null) {
//                following.remove(targetUser);
//            }
//            if (followers != null) {
//                followers.remove(currentUser);
//            }
//
//            transaction.update(currentUserRef, "followings", following);
//            transaction.update(targetUserRef, "followers", followers);
//
//            return null;
//        }).addOnSuccessListener(aVoid -> {
//            onSuccess.onSuccess(null);
//        }).addOnFailureListener(onFailure);
//    }
}
