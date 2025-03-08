package com.baobook.baobook.controller;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.UserHelper;
import com.example.baobook.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class UserHelperTest {
    private UserHelper userHelper;
    FirebaseFirestore db;

    private static final String username1 = "username1";
    private static final String username2 = "username2";

    @Before
    public void setUp() {
        userHelper = new UserHelper();
        db = FirebaseFirestore.getInstance();
        try {
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
            db.useEmulator("10.0.2.2", 8080);

            FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS);
            List<User> testUsers = new ArrayList<>(Arrays.asList(
                    new User(username1, "password"),
                    new User(username2, "password"))
            );

            for (User u : testUsers) {
                setUserInUsersCollection(u,
                        e -> {
                            throw new RuntimeException("Failed to instantiate test users.");
                        });
            }
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator.\n Is the emulator running? Use 'firebase emulators:start' to start the emulator.");
        }
    }

    @After
    public void tearDown() throws RuntimeException, InterruptedException {
        FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS);
    }

    @Test
    public void getUser_whenUserExists_shouldGetUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        userHelper.getUser(username1,
                user -> {
                    assertEquals(user.getUsername(), username1);
                    latch.countDown();
                },
                e -> {
                    fail(e.getMessage());
                    latch.countDown();
                });

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void getUser_whenUserDoesNotExist_shouldThrowRuntimeException() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        String testUser = "not in users";
        userHelper.getUser(testUser,
                user -> {
                    fail("Error should have been thrown when getting a non-existent user.");
                    latch.countDown();
                },
                e -> {
                    assertEquals(e.getMessage(), "User not found: " + testUser);
                    assertEquals(e.getClass(), RuntimeException.class);
                    latch.countDown();
                });

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void followUser_whenNotFollowingYet_shouldUpdateFollowingsAndFollowers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        userHelper.followUser(username1, username2,
                aVoid -> {
                    // Verify that:
                    // - username2 is in username1's followings list
                    // - username1 is in username2's followers list
                    verifyUserInList(username2, username1, FirestoreConstants.FIELD_FOLLOWINGS, latch);
                    verifyUserInList(username1, username2, FirestoreConstants.FIELD_FOLLOWERS, latch);
                },
                e -> {
                    fail(e.getMessage());
                    latch.countDown();
                    latch.countDown();
                }
        );

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void unfollowUser_whenFollowing_shouldUpdateFollowingsAndFollowers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);

        userHelper.followUser(username1, username2,
                aVoid -> {
                    // Setup: Assert that username1 follows username2
                    verifyUserInList(username1, username2, FirestoreConstants.FIELD_FOLLOWERS, latch);
                    verifyUserInList(username2, username1, FirestoreConstants.FIELD_FOLLOWINGS, latch);

                    // Unfollow
                    userHelper.unfollowUser(username1, username2,
                            aVoid2 -> {
                                // Assert that:
                                // - username2 is not in username1's followings list
                                // - username1 is not in username2's followers list
                                db.collection(FirestoreConstants.COLLECTION_USERS).document(username1).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            verifyUserNotInList(username2, username1, FirestoreConstants.FIELD_FOLLOWINGS, latch);
                                            verifyUserNotInList(username1, username2, FirestoreConstants.FIELD_FOLLOWERS, latch);
                                        })
                                        .addOnFailureListener(e -> {
                                            fail(e.getMessage());
                                            latch.countDown();
                                            latch.countDown();
                                            latch.countDown();
                                            latch.countDown();
                                        });
                            },
                            e -> fail(e.getMessage()));
                },
                e -> fail(e.getMessage())
        );

        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS));
    }

    private void verifyUserInList(String currentUser, String targetUser, String targetField, CountDownLatch latch) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Assert that currentUser is present in targetUser's targetField array.
                    List<String> targetList = (List<String>) documentSnapshot.get(targetField);
                    assertTrue(targetList.contains(currentUser));
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail(e.getMessage());
                    latch.countDown();
                });
    }

    private void verifyUserNotInList(String currentUser, String targetUser, String targetField, CountDownLatch latch) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(targetUser).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Assert that currentUser is present in targetUser's targetField array.
                    List<String> targetList = (List<String>) documentSnapshot.get(targetField);
                    assertFalse(targetList.contains(currentUser));
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail(e.getMessage());
                    latch.countDown();
                });
    }

    private void setUserInUsersCollection(User user, OnFailureListener onFailure) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(user.getUsername())
                .set(user)
                .addOnFailureListener(onFailure);
    }
}
