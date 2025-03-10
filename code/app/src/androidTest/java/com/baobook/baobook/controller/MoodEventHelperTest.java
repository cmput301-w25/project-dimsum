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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    private static final User user1 = new User(username1, "pw");
    private static final User following1 = new User(followingUsername1, "pw");
    private static final User following2 = new User(followingUsername2, "pw");

    MoodEvent moodEvent1 = new MoodEvent(
            username1,
            Mood.HAPPINESS,
            new Date("July 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "");

    MoodEvent moodEvent2 = new MoodEvent(
            followingUsername1,
            Mood.ANGER,
            new Date("August 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "");
    MoodEvent moodEvent3 = new MoodEvent(
            followingUsername2,
            Mood.ANGER,
            new Date("September 20, 2012"),
            Time.valueOf("00:00:00"),
            "",
            "");
    MoodEvent moodEvent4 = new MoodEvent(
            username1,
            Mood.ANGER,
            new Date("September 20, 2012"),
            Time.valueOf("00:00:00"),
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

            for (User u : testUsers) {
                addDocumentToCollection(
                        u,
                        FirestoreConstants.COLLECTION_USERS,
                        e -> {
                            throw new RuntimeException("Failed to instantiate test users.");
                        });
            }
            for (MoodEvent m : testMoodEvents) {
                addDocumentToCollection(
                        m,
                        FirestoreConstants.COLLECTION_MOOD_EVENTS,
                        e -> {
                            throw new RuntimeException("Failed to instantiate test moods.");
                        });
            }
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator. Is the Firebase emulator is running?");
        }
    }

    @Test
    public void getMoodEventsByUser_shouldReturnUsersMoodEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        List<MoodEvent> expected = new ArrayList<>(Arrays.asList(
                moodEvent1,
                moodEvent4
        ));

        moodEventHelper.getMoodEventsByUser(username1,
                moodEvents -> {
                    assertEquals(expected, moodEvents);
                    latch.countDown();
                },
                e -> {
                    fail(e.getMessage());
                    latch.countDown();
        });

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }



    private void addDocumentToCollection(Object document, String collectionName, OnFailureListener onFailure) {
        db.collection(collectionName)
                .add(document)
                .addOnFailureListener(onFailure);
    }
}
