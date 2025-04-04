package com.baobook.baobook.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.UserHelper;
import com.example.baobook.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class UserHelperTest {
    private UserHelper userHelper;
    private FirebaseFirestore db;
    private static final String username1 = "username1";
    private static final String username2 = "username2";

    @Before
    public void setUp() {
        userHelper = new UserHelper();
        db = FirebaseFirestore.getInstance();
        try {
            db.useEmulator("127.0.0.1", 8080);  // Use this if 10.0.2.2 is not working
            resetDatabase();
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator. Start it with 'firebase emulators:start'.");
        }
    }

    @After
    public void tearDown() {
        resetDatabase();
    }

    @Test
    public void getUser_whenUserExists_shouldGetUser() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<User> future = new CompletableFuture<>();

        userHelper.getUser(username1, future::complete, future::completeExceptionally);

        User user = future.get(5, TimeUnit.SECONDS);
        assertEquals(username1, user.getUsername());
    }

    @Test
    public void getUser_whenUserDoesNotExist_shouldThrowRuntimeException() {
        CompletableFuture<Exception> future = new CompletableFuture<>();
        String testUser = "not in users";

        userHelper.getUser(testUser,
                user -> future.completeExceptionally(new RuntimeException("User should not exist")),
                future::complete
        );

        try {
            Exception e = future.get(5, TimeUnit.SECONDS);
            assertEquals("User not found: " + testUser, e.getMessage());
            assertEquals(RuntimeException.class, e.getClass());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void followUser_whenNotFollowingYet_shouldUpdateFollowingsAndFollowers() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> future = new CompletableFuture<>();

        userHelper.followUser(username1, username2,
                aVoid -> {
                    CompletableFuture<Void> verify = verifyUserInSubcollection(username1, username2, FirestoreConstants.COLLECTION_FOLLOWINGS)
                            .thenCombine(verifyUserInSubcollection(username2, username1, FirestoreConstants.COLLECTION_FOLLOWERS), (a, b) -> null);

                    verify.whenComplete((result, ex) -> {
                        if (ex != null) future.completeExceptionally(ex);
                        else future.complete(null);
                    });
                },
                future::completeExceptionally
        );

        future.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void unfollowUser_whenFollowing_shouldUpdateFollowingsAndFollowers() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> future = new CompletableFuture<>();

        userHelper.followUser(username1, username2,
                aVoid -> {
                    CompletableFuture<Void> verifyBeforeUnfollow = verifyUserInSubcollection(username1, username2, FirestoreConstants.COLLECTION_FOLLOWINGS)
                            .thenCombine(verifyUserInSubcollection(username2, username1, FirestoreConstants.COLLECTION_FOLLOWERS), (a, b) -> null);

                    verifyBeforeUnfollow.thenRun(() -> {
                        userHelper.unfollowUser(username1, username2,
                                aVoid2 -> {
                                    CompletableFuture<Void> verifyAfterUnfollow = verifyUserNotInSubcollection(username1, username2, FirestoreConstants.COLLECTION_FOLLOWINGS)
                                            .thenCombine(verifyUserNotInSubcollection(username2, username1, FirestoreConstants.COLLECTION_FOLLOWERS), (a, b) -> null);

                                    verifyAfterUnfollow.whenComplete((result, ex) -> {
                                        if (ex != null) future.completeExceptionally(ex);
                                        else future.complete(null);
                                    });
                                },
                                future::completeExceptionally);
                    }).exceptionally(ex -> {
                        future.completeExceptionally(ex);
                        return null;
                    });
                },
                future::completeExceptionally
        );

        future.get(10, TimeUnit.SECONDS);
    }
    @Test
    public void checkFollowStatus_whenFollowing_shouldReturnTrue() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Object> future = new CompletableFuture<>();

        // Set up following status
        userHelper.followUser(username1, username2,
                aVoid -> userHelper.checkFollowStatus(username1, username2, future::complete, future::completeExceptionally),
                future::completeExceptionally);

        Pair<Boolean, Boolean> result = (Pair<Boolean, Boolean>) future.get(5, TimeUnit.SECONDS);
        assertEquals(true, result.first); // isFollowing
        assertEquals(false, result.second); // hasRequested
    }

    @Test
    public void checkFollowStatus_whenNotFollowing_shouldReturnFalse() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Pair<Boolean, Boolean>> future = new CompletableFuture<>();

        userHelper.checkFollowStatus(username1, username2, future::complete, future::completeExceptionally);

        Pair<Boolean, Boolean> result = future.get(5, TimeUnit.SECONDS);
        assertEquals(false, result.first); // isFollowing
        assertEquals(false, result.second); // hasRequested
    }

    @Test
    public void checkFollowStatus_whenRequestPending_shouldReturnHasRequested() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Pair<Boolean, Boolean>> future = new CompletableFuture<>();

        // Simulate a follow request (not accepted yet)
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username2)
                .collection(FirestoreConstants.COLLECTION_REQUESTS)
                .document(username1)
                .set(new Object())
                .addOnSuccessListener(aVoid -> userHelper.checkFollowStatus(username1, username2, future::complete, future::completeExceptionally));

        Pair<Boolean, Boolean> result = future.get(5, TimeUnit.SECONDS);
        assertEquals(false, result.first); // isFollowing
        assertEquals(true, result.second); // hasRequested
    }


    private CompletableFuture<Void> verifyUserInList(String currentUser, String targetUser, String targetField) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> targetList = (List<String>) documentSnapshot.get(targetField);
                    if (targetList.contains(currentUser)) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new AssertionError("User not found in list"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private CompletableFuture<Void> verifyUserNotInList(String currentUser, String targetUser, String targetField) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> targetList = (List<String>) documentSnapshot.get(targetField);
                    if (!targetList.contains(currentUser)) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new AssertionError("User should not be in the list"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private CompletableFuture<Void> setUserInUsersCollection(User user) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(FirestoreConstants.COLLECTION_USERS).document(user.getUsername())
                .set(user)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private void resetDatabase() {
        FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS).join();
        List<User> testUsers = Arrays.asList(
                new User(username1, "password", 0 ,0 ,0),
                new User(username2, "password", 0 ,0 ,0)
        );
        for (User user : testUsers) {
            setUserInUsersCollection(user).join();
        }
    }
    private CompletableFuture<Void> verifyUserInSubcollection(String ownerUsername, String targetUsername, String subcollection) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(ownerUsername)
                .collection(subcollection)
                .document(targetUsername)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new AssertionError("User not found in subcollection: " + subcollection));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private CompletableFuture<Void> verifyUserNotInSubcollection(String ownerUsername, String targetUsername, String subcollection) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(ownerUsername)
                .collection(subcollection)
                .document(targetUsername)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new AssertionError("User still found in subcollection: " + subcollection));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }


}
