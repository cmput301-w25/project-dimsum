package com.baobook.baobook.controller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.controller.UserHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.SocialSetting;
import com.example.baobook.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONStringer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class MoodEventHelperTest {
    private MoodEventHelper moodEventHelper;
    FirebaseFirestore db;

    // have a user
    // have the user's following: two users
    // mood events hat belong to the user, mood events that belong to the following users

    private static final String username1 = "user1";
    private static final String followingUsername1 = "following1";
    private static final String followingUsername2 = "following2";

    private static final User following1 = new User(followingUsername1, "");
    private static final User following2 = new User(followingUsername2, "");
    private static final User user1 = new User(username1, "");

    private static final UserHelper userHelper = new UserHelper();

    MoodEvent moodEvent1 = new MoodEvent(
            username1,
            "1",
            Mood.HAPPINESS,
            new Date("July 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "",
            "");

    MoodEvent moodEvent2 = new MoodEvent(
            followingUsername1,
            "2",
            Mood.ANGER,
            new Date("August 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "",
            "");

    MoodEvent moodEvent3 = new MoodEvent(
            followingUsername2,
            "3",
            Mood.ANGER,
            new Date("September 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "",
            "");
    MoodEvent moodEvent4 = new MoodEvent(
            username1,
            "4",
            Mood.ANGER,
            new Date("September 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "",
            "");

    @Before
    public void setUp() {
        moodEventHelper = new MoodEventHelper();
        db = FirebaseFirestore.getInstance();
        try {
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
            db.useEmulator("10.0.2.2", 8080);

            FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS);
            FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_MOOD_EVENTS);
            List<User> testUsers = new ArrayList<>(Arrays.asList(
                    user1,
                    following1,
                    following2
            ));
            List<MoodEvent> testMoodEvents = new ArrayList<>(Arrays.asList(
                    moodEvent1,
                    moodEvent2,
                    moodEvent3,
                    moodEvent4
            ));

            // Set up following relationships
            user1.followUser(following1);
            user1.followUser(following2);

            // Initialize Firestore
            for (User u : testUsers) {
                addDocumentToCollection(u, FirestoreConstants.COLLECTION_USERS)
                        .exceptionally(ex -> {
                            System.err.println("Error initializing test environment: " + ex.getMessage());
                            return null;
                        });
            }
            for (MoodEvent m : testMoodEvents) {
                addDocumentToCollection(m, FirestoreConstants.COLLECTION_MOOD_EVENTS)
                        .exceptionally(ex -> {
                            System.err.println("Error initializing test environment: " + ex.getMessage());
                            return null;
                        });
            }
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator. Is the Firebase emulator is running?");
        }
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

    private CompletableFuture<Void> addDocumentToCollection(Object document, String collectionName) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(collectionName)
                .add(document)
                .addOnSuccessListener(aVoid -> future.complete(null)) // Complete successfully
                .addOnFailureListener(future::completeExceptionally); // Complete with an exception

        return future;
    }
}
