package com.example.baobook.controller;

import android.util.Pair;

import com.example.baobook.R;
import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Handles users and user interactions, following and unfollowing users.
 */
public class UserHelper {
    private final FirebaseFirestore db;

    public UserHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Gets a User.
     * @param username  The username of the User to get.
     * @param onSuccess  Callback that is triggered when retrieval is successful.
     * @param onFailure  Callback that is triggered when an error occurs.
     */
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

    /**function that checks the follow status of a user, whether they are following or have requested to follow them
     *
     * @param currentUser current logged in user
     * @param targetUser user to check follow status of
     * @param onSuccess callback to run when operation is successful
     * @param onFailure callback to run when operation fails
     */

    public void checkFollowStatus(String currentUser, String targetUser,
                                  OnSuccessListener<Pair<Boolean, Boolean>> onSuccess,
                                  OnFailureListener onFailure) {
        Task<DocumentSnapshot> followingTask = db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(currentUser)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .document(targetUser)
                .get();

        Task<DocumentSnapshot> requestTask = db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(targetUser)
                .collection(FirestoreConstants.COLLECTION_REQUESTS)
                .document(currentUser)
                .get();

        Tasks.whenAllSuccess(followingTask, requestTask)
                .addOnSuccessListener(results -> {
                    boolean isFollowing = ((DocumentSnapshot) results.get(0)).exists();
                    boolean hasRequested = ((DocumentSnapshot) results.get(1)).exists();
                    onSuccess.onSuccess(new Pair<>(isFollowing, hasRequested));
                })
                .addOnFailureListener(e ->
                        onFailure.onFailure(new RuntimeException(String.format("Error getting follow status for %s: %s", currentUser, e.getMessage())))
                );
    }

    /** refactored method is in FirestoreHelper
     * Allows a user to follow another user by updating the "followings" and "followers" lists in Firestore.
     *
     * @param currentUser The username of the user who wants to follow another user.
     * @param targetUser The username of the user being followed.
     * @param onSuccess Callback that is triggered when the operation completes successfully.
     * @param onFailure Callback that is triggered when an error occurs.
     *
     * @throws RuntimeException if one or both users do not exist in Firestore.
     */
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

    /**
     * Allows a user to unfollow another user by updating the "followings" and "followers" lists in Firestore.
     *
     * @param currentUser The username of the user who wants to unfollow another user.
     * @param targetUser The username of the user being unfollowed.
     * @param onSuccess Callback that is triggered when the operation completes successfully.
     * @param onFailure Callback that is triggered when an error occurs.
     *
     * @throws RuntimeException if one or both users do not exist in Firestore.
     */
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

    public int getProfilePicture(Integer level) {
        if (level >= 7) {
            level = 6;
        }
        int[] pictures = {
                R.drawable.baobun2,
                R.drawable.baobun3,
                R.drawable.baobun4,
                R.drawable.baobun5,
                R.drawable.baobun6,
                R.drawable.baobun7
        };
        return pictures[level-1];
    }

}
