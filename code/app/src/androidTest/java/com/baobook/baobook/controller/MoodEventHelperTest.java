package com.baobook.baobook.controller;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.Privacy;
import com.example.baobook.model.SocialSetting;
import com.example.baobook.model.User;
import com.example.baobook.model.Comment;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.w3c.dom.Comment;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
            "", Privacy.PUBLIC);

    private static final MoodEvent moodEvent2 = new MoodEvent(
            followingUsername1,
            "2",
            Mood.ANGER,
            OffsetDateTime.of(2012, 8, 20, 3, 2, 0, 0, ZoneOffset.UTC),

            "",
            SocialSetting.ALONE,
            "", Privacy.PUBLIC);
    private static final MoodEvent moodEvent3 = new MoodEvent(
            followingUsername2,
            "3",
            Mood.ANGER,
            OffsetDateTime.of(2012, 9, 20, 3, 2, 0, 0, ZoneOffset.UTC),
            "",
            SocialSetting.ALONE,
            "", Privacy.PUBLIC);
    private static final MoodEvent moodEvent4 = new MoodEvent(
            username1,
            "4",
            Mood.ANGER,
            OffsetDateTime.of(2012, 9, 20, 3, 2, 0, 0, ZoneOffset.UTC),
            "",
            SocialSetting.ALONE,
            "", Privacy.PUBLIC);

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
            Log.e("MoodEventHelper", "connected to emulator");
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
        Log.e("MoodEventHelper", "getMoodEventsByUser_shouldReturnUsersMoodEvents");
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
        List<MoodEvent> expected = Arrays.asList(moodEvent3, moodEvent2);

        // Set up Firestore data
        Task<Void> setMoodEvent2 = db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document("2")
                .set(moodEvent2);

        Task<Void> setMoodEvent3 = db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document("3")
                .set(moodEvent3);

        Task<Void> setFollowing1 = db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username1)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .document("following1")
                .set(Collections.emptyMap());

        Task<Void> setFollowing2 = db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(username1)
                .collection(FirestoreConstants.COLLECTION_FOLLOWINGS)
                .document("following2")
                .set(Collections.emptyMap());

        // Ensure all tasks are completed before proceeding
        Tasks.whenAll(setMoodEvent2, setMoodEvent3, setFollowing1, setFollowing2)
                .addOnSuccessListener(aVoid -> {
                    moodEventHelper.getMoodEventsByFollowing(username1,
                            future::complete,
                            future::completeExceptionally
                    );
                })
                .addOnFailureListener(future::completeExceptionally);

        // Wait for the result and assert
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
                "", Privacy.PUBLIC
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
                "", Privacy.PUBLIC
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
    @Test
    public void loadComments_shouldReturnCommentsForMoodEvent() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<Comment>> future = new CompletableFuture<>();
        String moodEventId = "1";

        User user2 = new User("user2", "");
        User user3 = new User("user3", "");

        // Create test comments
        Comment comment1 = new Comment(moodEvent1.getId(), user1,"This is a great post!");
        Comment comment2 = new Comment(moodEvent1.getId(), user2,"I totally agree!");
        Comment comment3 = new Comment(moodEvent1.getId(), user3, "Thanks for sharing!");

        List<Comment> expectedComments = Arrays.asList(comment1, comment2, comment3);

        // Add comments to Firestore
        CompletableFuture<Void> addCommentsFuture = new CompletableFuture<>();
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document(moodEvent1.getId())
                .collection(FirestoreConstants.COLLECTION_COMMENTS)
                .document("comment1").set(comment1)
                .continueWithTask(task -> db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                        .document(moodEventId)
                        .collection(FirestoreConstants.COLLECTION_COMMENTS)
                        .document("comment2").set(comment2))
                .continueWithTask(task -> db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                        .document(moodEventId)
                        .collection(FirestoreConstants.COLLECTION_COMMENTS)
                        .document("comment3").set(comment3))
                .addOnSuccessListener(aVoid -> addCommentsFuture.complete(null))
                .addOnFailureListener(addCommentsFuture::completeExceptionally);

        addCommentsFuture.get(5, TimeUnit.SECONDS);

        // Call loadComments to retrieve comments
        moodEventHelper.loadComments(moodEventId,
                future::complete,
                future::completeExceptionally
        );

        // Verify comments
        List<Comment> actualComments = future.get(5, TimeUnit.SECONDS);

        assertEquals(3, actualComments.size());
        assertEquals(expectedComments.get(0).getText(), actualComments.get(0).getText());
        assertEquals(expectedComments.get(1).getText(), actualComments.get(1).getText());
        assertEquals(expectedComments.get(2).getText(), actualComments.get(2).getText());
    }
    @Test
    public void addComment_shouldAddCommentToFirestore() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Comment comment = new Comment(moodEvent1.getId(), user1, "This is a great post!");


        // Call the function to add the comment
        moodEventHelper.addComment(moodEvent1.getId(), comment,
                aVoid -> future.complete(null),
                future::completeExceptionally
        );

        // Wait for the Firestore write to complete
        future.get(5, TimeUnit.SECONDS);

        // Verify the comment was added to Firestore
        CompletableFuture<DocumentSnapshot> verificationFuture = new CompletableFuture<>();
        db.collection(FirestoreConstants.COLLECTION_MOOD_EVENTS)
                .document(moodEvent1.getId())
                .collection(FirestoreConstants.COLLECTION_COMMENTS)
                .whereEqualTo("text", comment.getText())
                .whereEqualTo("username", comment.getAuthorUsername())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        verificationFuture.completeExceptionally(new AssertionError("Comment was not added to Firestore."));
                    } else {
                        verificationFuture.complete(querySnapshot.getDocuments().get(0));
                    }
                })
                .addOnFailureListener(verificationFuture::completeExceptionally);

        DocumentSnapshot documentSnapshot = verificationFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(String.valueOf(documentSnapshot), "Comment should exist in Firestore");
        Comment storedComment = documentSnapshot.toObject(Comment.class);
        assertNotNull(String.valueOf(storedComment), "Stored comment should not be null");
        assertEquals(comment.getText(), storedComment.getText(), "Comment text should match");
        assertEquals(comment.getAuthorUsername(), storedComment.getAuthorUsername(), "Comment username should match");
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
