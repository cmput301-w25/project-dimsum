package com.baobook.baobook.controller;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.SocialSetting;
import com.example.baobook.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class MoodEventHelperTest {
    private MoodEventHelper moodEventHelper;
    FirebaseFirestore db;

    private static final String username1 = "user1";
    private static final String followingUsername1 = "following1";
    private static final String followingUsername2 = "following2";
    private static final User following1 = new User(followingUsername1, "");
    private static final User following2 = new User(followingUsername2, "");
    private static final User user1 = new User(username1, "");
    private static final MoodEvent moodEvent1 = new MoodEvent(
            username1,
            "1",
            Mood.HAPPINESS,
            OffsetDateTime.of(2012, 7, 20, 3, 2, 0, 0, ZoneOffset.UTC),
            "",
            SocialSetting.ALONE,
            "");

    private static final MoodEvent moodEvent2 = new MoodEvent(
            followingUsername1,
            "2",
            Mood.ANGER,
            OffsetDateTime.of(2012, 8, 20, 3, 2, 0, 0, ZoneOffset.UTC),

            "",
            SocialSetting.ALONE,
            "");
    private static final MoodEvent moodEvent3 = new MoodEvent(
            followingUsername2,
            "3",
            Mood.ANGER,
            OffsetDateTime.of(2012, 9, 20, 3, 2, 0, 0, ZoneOffset.UTC),
            "",
            SocialSetting.ALONE,
            "");
    private static final MoodEvent moodEvent4 = new MoodEvent(
            username1,
            "4",
            Mood.ANGER,
            OffsetDateTime.of(2012, 9, 20, 3, 2, 0, 0, ZoneOffset.UTC),
            "",
            SocialSetting.ALONE,
            "");

    private static final List<User> testUsers = new ArrayList<>(Arrays.asList(
            user1,
            following1,
            following2
    ));
    private static final List<MoodEvent> testMoodEvents = new ArrayList<>(Arrays.asList(
            moodEvent1,
            moodEvent2,
            moodEvent3,
            moodEvent4
    ));

    @Before
    public void setUp() {
        moodEventHelper = new MoodEventHelper();

        // Set up following relationships
        user1.followUser(following1);
        user1.followUser(following2);

        db = FirebaseFirestore.getInstance();
        try {
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
            db.useEmulator("10.0.2.2", 8080);
            resetDatabase();
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator. Is the Firebase emulator is running?");
        }
    }

    @After
    public void tearDown() {
        resetDatabase();
    }

    @Test
    public void getMoodEventsByUser_shouldReturnUsersMoodEvents() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<MoodEvent>> future = new CompletableFuture<>();

        List<MoodEvent> expected = new ArrayList<>(Arrays.asList(
                moodEvent4,  // Newest first
                moodEvent1
        ));

        moodEventHelper.getMoodEventsByUser(username1,
                future::complete,
                future::completeExceptionally
        );

        // Waits for the result and asserts
        List<MoodEvent> moodEvents = future.get(5, TimeUnit.SECONDS);
        assertEquals(expected, moodEvents);
    }

    @Test
    public void getMoodEventsByFollowing_shouldReturnFollowingMoodEvents() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<MoodEvent>> future = new CompletableFuture<>();

        List<MoodEvent> expected = new ArrayList<>(Arrays.asList(
                moodEvent3,  // Newest first
                moodEvent2
        ));

        moodEventHelper.getMoodEventsByFollowing(username1,
                future::complete,
                future::completeExceptionally
        );

        // Waits for the result and asserts
        List<MoodEvent> moodEvents = future.get(5, TimeUnit.SECONDS);
        assertEquals(expected, moodEvents);
    }

    @Test
    public void publishMood_shouldPublishMoodToFirestore() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> future = new CompletableFuture<>();

        MoodEvent moodEvent = new MoodEvent(
                "user1",
                "5",
                Mood.HAPPINESS,
                OffsetDateTime.of(2025, 3, 10, 3, 2, 0, 0, ZoneOffset.UTC),
                "",
                SocialSetting.ALONE,
                ""
        );

        moodEventHelper.publishMood(moodEvent,
                aVoid -> {
                    // Verify that the MoodEvent is published.
                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(moodEvent.getId()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    future.complete(null);
                                }
                                future.completeExceptionally(new AssertionError("MoodEvent not published to Firestore."));
                            }).addOnFailureListener(future::completeExceptionally);
                },
                future::completeExceptionally
        );

        future.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void publishMood_whenMoodExists_shouldThrowRuntimeException() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Exception> future = new CompletableFuture<>();

        moodEventHelper.publishMood(moodEvent1,
                aVoid -> future.completeExceptionally(new AssertionError("Publish mood succeeded for an already existing MoodEvent.")),
                future::complete
        );

        assertEquals(future.get().getMessage(), "Mood event already exists: " + moodEvent1);
        assertEquals(future.get().getClass(), RuntimeException.class);
        future.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void updateMood_shouldUpdateMoodinFirestore() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<MoodEvent> future = new CompletableFuture<>();

        MoodEvent moodEvent = moodEvent1;
        moodEvent.setDescription("updated!");

        moodEventHelper.updateMood(moodEvent,
                aVoid -> {
                    // Verify that the MoodEvent is published.
                    db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(moodEvent.getId()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    future.complete(documentSnapshot.toObject(MoodEvent.class));
                                }
                                future.completeExceptionally(new AssertionError("MoodEvent not published to Firestore."));
                            }).addOnFailureListener(future::completeExceptionally);
                },
                future::completeExceptionally
        );

        assertEquals(moodEvent, future.get());
        future.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void deleteMood_shouldRemoveMoodEventFromFirestore() throws Exception {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String moodId = "testMood1";

        MoodEvent moodEvent = new MoodEvent(
                "user1",
                moodId,
                Mood.HAPPINESS,
                OffsetDateTime.of(2025, 3, 10, 3, 2, 0, 0, ZoneOffset.UTC),
                "Feeling happy!",
                SocialSetting.ALONE,
                ""
        );

        // Add the MoodEvent to Firestore first
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(moodId)
                .set(moodEvent)
                .addOnSuccessListener(aVoid -> {
                    // Delete the MoodEvent
                    moodEventHelper.deleteMood(moodEvent,
                            aVoid2 -> {
                                // Verify the mood event is deleted
                                db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS).document(moodId).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            assertFalse("MoodEvent should be deleted", documentSnapshot.exists());
                                            future.complete(null);
                                        })
                                        .addOnFailureListener(future::completeExceptionally);
                            },
                            future::completeExceptionally);
                })
                .addOnFailureListener(future::completeExceptionally);

        future.get(5, TimeUnit.SECONDS);
    }


    private CompletableFuture<Void> addDocumentToCollection(Object document, String uniqueId, String collectionName) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(collectionName)
                .document(uniqueId)
                .set(document)
                .addOnSuccessListener(aVoid -> future.complete(null)) // Complete successfully
                .addOnFailureListener(future::completeExceptionally); // Complete with an exception

        return future;
    }

    private void resetDatabase() {
        FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS).join();
        FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_MOOD_EVENTS).join();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // Initialize Firestore
        for (User u : testUsers) {
            futures.add(addDocumentToCollection(u, u.getUsername(), FirestoreConstants.COLLECTION_USERS)
                    .exceptionally(ex -> {
                        throw new RuntimeException(ex.getMessage());
                    }));
        }
        for (MoodEvent m : testMoodEvents) {
            futures.add(addDocumentToCollection(m, m.getId(), FirestoreConstants.COLLECTION_MOOD_EVENTS)
                    .exceptionally(ex -> {
                        throw new RuntimeException(ex.getMessage());
                    }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
