package com.example.baobook.controller;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * helps retrive user profiles, followers, following
 */
public class UserHelper {
    private final FirebaseFirestore db;

    public UserHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getUser(String username, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new RuntimeException("User not found: " + username));
                    }
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(new RuntimeException(String.format("Error getting user: %s. %s", username, e.getMessage())));
                });
    }

    public void followUser(String currentUser, String targetUser, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        DocumentReference currentUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(currentUser);
        DocumentReference targetUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser);

        db.runTransaction(transaction -> {
            DocumentSnapshot currentUserDoc = transaction.get(currentUserRef);
            DocumentSnapshot targetUserDoc = transaction.get(targetUserRef);

            if (!currentUserDoc.exists() || !targetUserDoc.exists()) {
                throw new RuntimeException("One or both users do not exist.");
            }

            List<String> following = (List<String>) currentUserDoc.get("followings");
            List<String> followers = (List<String>) targetUserDoc.get("followers");

            if (following != null && !following.contains(targetUser)) {
                following.add(targetUser);
            }
            if (followers != null && !followers.contains(currentUser)) {
                followers.add(currentUser);
            }

            transaction.update(currentUserRef, "followings", following);
            transaction.update(targetUserRef, "followers", followers);

            return null;
        }).addOnSuccessListener(aVoid -> {
            onSuccess.onSuccess(null);
        }).addOnFailureListener(onFailure);
    }

    public void unfollowUser(String currentUser, String targetUser, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        DocumentReference currentUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(currentUser);
        DocumentReference targetUserRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser);

        db.runTransaction(transaction -> {
            DocumentSnapshot currentUserDoc = transaction.get(currentUserRef);
            DocumentSnapshot targetUserDoc = transaction.get(targetUserRef);

            if (!currentUserDoc.exists() || !targetUserDoc.exists()) {
                throw new RuntimeException("One or both users do not exist.");
            }

            List<String> following = (List<String>) currentUserDoc.get("followings");
            List<String> followers = (List<String>) targetUserDoc.get("followers");

            if (following != null) {
                following.remove(targetUser);
            }
            if (followers != null) {
                followers.remove(currentUser);
            }

            transaction.update(currentUserRef, "followings", following);
            transaction.update(targetUserRef, "followers", followers);

            return null;
        }).addOnSuccessListener(aVoid -> {
            onSuccess.onSuccess(null);
        }).addOnFailureListener(onFailure);
    }
}
