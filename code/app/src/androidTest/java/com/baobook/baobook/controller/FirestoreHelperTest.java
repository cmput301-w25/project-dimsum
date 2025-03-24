package com.baobook.baobook.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for FirestoreHelper.
 */
@RunWith(AndroidJUnit4.class)
public class FirestoreHelperTest {
    FirebaseFirestore db;
    private FirestoreHelper firestoreHelper;
    private static final User user1 = new User("user1");
    private static final User user2 = new User("user2");
    Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        db = FirebaseFirestore.getInstance();
        try {
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
            db.useEmulator("10.0.2.2", 8080);
            FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS).join();
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator.\n Is the emulator running? Use 'firebase emulators:start' to start the emulator.");
        }
    }
    @After
    public void tearDown() throws RuntimeException, InterruptedException {
        FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS).join();
    }
    @Test
    public void testRequestFollow() throws InterruptedException {
        // add user'1 to user 2's requests
        FirestoreHelper.requestFollow(user1, user2, context);
        //verify data
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user2.getUsername())
                .collection(FirestoreConstants.COLLECTION_REQUESTS)
                .document(user1.getUsername())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    assertNotNull(documentSnapshot);
                    assertEquals(user1.getUsername(), documentSnapshot.getId());
                })
                .addOnFailureListener(e -> fail("Failed to retrieve user data"));
    }
    @Test
    public void testLoadFollow_Success() throws InterruptedException {
        //add user2 to user1's followers
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user1.getUsername())
                .collection(FirestoreConstants.COLLECTION_FOLLOWERS)
                .document(user2.getUsername())
                .set(user2);
        //add user1 to user2's followings
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user2.getUsername())
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .document(user1.getUsername())
                .set(user1);

        FirestoreHelper.loadFollow(user1.getUsername(), FirestoreConstants.COLLECTION_FOLLOWERS,
                users -> {
                    assertNotNull(users);
                    assertEquals(1, users.size());
                    assertEquals(user2.getUsername(), users.get(0).getUsername());
                },
                e -> fail("Failed to load followers"));
        //ensure user1 is in user 2's followings
        FirestoreHelper.loadFollow(user1.getUsername(), FirestoreConstants.COLLECTION_FOLLOWINGS,
                users -> {
                    assertNotNull(users);
                    assertEquals(1, users.size());
                    assertEquals(user1.getUsername(), users.get(0).getUsername());
                },
                e -> fail("Failed to load followings"));
    }
    @Test
    public void testLoadFollow_NoFollowers() {
    FirestoreHelper.loadFollow(user1.getUsername(), FirestoreConstants.COLLECTION_FOLLOWERS,
            users -> {
                assertNotNull(users);
                assertEquals(0, users.size());
            }, e -> fail("No followers"));
    }
    @Test
    public void testLoadFollow_NoFollowings() {
        FirestoreHelper.loadFollow(user1.getUsername(), FirestoreConstants.COLLECTION_FOLLOWINGS,
                users -> {
                    assertNotNull(users);
                    assertEquals(0, users.size());
                }, e -> fail("No followings"));
    }
    @Test
    public void testLoadFollow_NoRequests(){
        FirestoreHelper.loadFollow(user1.getUsername(), FirestoreConstants.COLLECTION_REQUESTS,
                users -> {
                    assertNotNull(users);
                    assertEquals(0, users.size());
                }, e -> fail("No requests"));
    }
    @Test
    public void testSearchUsers_Success() throws InterruptedException {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user1.getUsername())
                .set(user1);
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user2.getUsername())
                .set(user2);
        FirestoreHelper.searchUsers("user1", users -> {
            assertNotNull(users);
            assertEquals(1, users.size());
            assertEquals(user1.getUsername(), users.get(0).getUsername());
        });
        FirestoreHelper.searchUsers("user", users -> {
            assertNotNull(users);
            assertEquals(2, users.size());
            assertEquals(user1.getUsername(), users.get(0).getUsername());
        });
    }
    @Test
    public void testSearchUsers_Failure() throws InterruptedException {
        FirestoreHelper.searchUsers("invalidUser", users -> {
            assertNotNull(users);
            assertEquals(0, users.size());
        });
    }
    @Test
    public void testSearchUsers_CaseInsensitive() throws InterruptedException {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user1.getUsername())
                .set(user1);
        FirestoreHelper.searchUsers("user1", users -> {
            assertNotNull(users);
            assertEquals(1, users.size());
            assertEquals(user1.getUsername(), users.get(0).getUsername());
        });

    }

    @Test
    public void testUnfollow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Add user2 to user1's followers
        Task<Void> addFollower = db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user1.getUsername())
                .collection(FirestoreConstants.COLLECTION_FOLLOWERS)
                .document(user2.getUsername())
                .set(user2);

        // Add user1 to user2's followings
        Task<Void> addFollowing = db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user2.getUsername())
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .document(user1.getUsername())
                .set(user1);

        // Ensure both writes are completed
        Tasks.whenAll(addFollower, addFollowing)
                .addOnSuccessListener(aVoid -> {
                    // Call the unfollow function
                    FirestoreHelper.unfollow(user1, user2, context);

                    // Verify that user2 is removed from user1's followers
                    db.collection(FirestoreConstants.COLLECTION_USERS)
                            .document(user1.getUsername())
                            .collection(FirestoreConstants.COLLECTION_FOLLOWERS)
                            .document(user2.getUsername())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                assertFalse(documentSnapshot.exists());

                                // Verify that user1 is removed from user2's followings
                                db.collection(FirestoreConstants.COLLECTION_USERS)
                                        .document(user2.getUsername())
                                        .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                                        .document(user1.getUsername())
                                        .get()
                                        .addOnSuccessListener(followingSnapshot -> {
                                            assertFalse(followingSnapshot.exists());
                                            latch.countDown(); // Test complete
                                        })
                                        .addOnFailureListener(e -> {
                                            fail("Failed to retrieve followings: " + e.getMessage());
                                            latch.countDown();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                fail("Failed to retrieve followers: " + e.getMessage());
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> fail("Failed to set initial follow state: " + e.getMessage()));

        assertTrue(latch.await(10, TimeUnit.SECONDS)); // Wait for async calls to complete
    }

    // still need to test firestore mood
}
