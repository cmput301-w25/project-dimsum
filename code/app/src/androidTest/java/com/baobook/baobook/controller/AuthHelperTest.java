package com.baobook.baobook.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.baobook.constant.SharedPreferencesConstants;
import com.example.baobook.controller.AuthHelper;
import com.example.baobook.constant.FirestoreConstants;
import com.example.baobook.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AuthHelperTest {
    private AuthHelper authHelper;
    private SharedPreferences sharedPreferences;
    FirebaseFirestore db;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.USER_SESSION, Context.MODE_PRIVATE);
        authHelper = new AuthHelper(context);
        db = FirebaseFirestore.getInstance();
        try {
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
            db.useEmulator("10.0.2.2", 8080);
            FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS);
        } catch (IllegalStateException e) {
            // pass
        } catch (Exception e) {
            fail("Failed to setup Firebase emulator.\n Is the emulator running? Use 'firebase emulators:start' to start the emulator.");
        }
    }

    @After
    public void tearDown() throws RuntimeException, InterruptedException {
        // Clear SharedPreferences.
        sharedPreferences.edit().clear().apply();
        FirestoreTestUtils.clearFirestoreCollection(FirestoreConstants.COLLECTION_USERS);
    }

    @Test
    public void registerUser_shouldStoreInFirestoreAndSharedPreferences() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String testUsername = "user";
        String testPassword = "password";

        authHelper.registerUser(testUsername, testPassword,
                aVoid -> {
                    // Verify that the user exists in Firestore.
                    db.collection(FirestoreConstants.COLLECTION_USERS).document(testUsername).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                assertTrue(documentSnapshot.exists());
                                assertEquals(testUsername, documentSnapshot.getString(FirestoreConstants.FIELD_USERNAME));
                                assertEquals(testPassword, documentSnapshot.getString(FirestoreConstants.FIELD_PASSWORD));

                                // Verify that UserSession is updated accordingly in SharedPreferences.
                                assertEquals(testUsername, sharedPreferences.getString(SharedPreferencesConstants.USERNAME, null));
                                assertTrue(sharedPreferences.getBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false));
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                fail(e.getMessage());
                                latch.countDown();
                            });
                },
                e -> {
                    fail(e.getMessage());
                    latch.countDown();
                }
        );
        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void registerUser_shouldFailForDuplicateUsername() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String testUsername = "existingUser";
        String testPassword = "password";

        // Add the user to Firestore.
        db.collection(FirestoreConstants.COLLECTION_USERS).document(testUsername)
                .set(new User(testUsername, testPassword))
                .addOnSuccessListener(aVoid -> {
                    // Attempt to register a user with the same username.
                    authHelper.registerUser(testUsername, "differentPassword",
                            aVoid2 -> fail("User registration should have failed for duplicate username."),
                            e -> assertEquals("Attempted to register a user with a taken username", e.getMessage())
                    );
                    latch.countDown();
                });

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void loginUser_shouldSucceedForCorrectLoginAndStoreInSharedPreferences() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String username = "user";
        String password = "password";

        // Add the user to Firestore.
        db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                .set(new User(username, password))
                .addOnSuccessListener(aVoid -> {
                    // Log in with the same login.
                    authHelper.loginUser(username, password,
                            aVoid2 -> {
                                // Verify that UserSession is updated accordingly in SharedPreferences.
                                assertEquals(username, sharedPreferences.getString(SharedPreferencesConstants.USERNAME, null));
                                assertTrue(sharedPreferences.getBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false));
                            },
                            e -> fail(e.getMessage())
                    );
                    latch.countDown();
                });

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void loginUser_shouldFailForIncorrectPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String username = "user";
        String password = "password";

        // Add the user to Firestore.
        db.collection(FirestoreConstants.COLLECTION_USERS).document(username)
                .set(new User(username, password))
                .addOnSuccessListener(aVoid -> {
                    // Log in with the same login.
                    authHelper.loginUser(username, "differentPassword",
                            aVoid2 -> fail("Login succeeded for incorrect password."),
                            e -> assertEquals("Incorrect password for user: " + username, e.getMessage())
                    );
                    latch.countDown();
                });

        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
    }

    public void logoutUser_shouldEndUserSessionInSharedPreferences() {
        String username = "user";
        String password = "password";
        authHelper.registerUser(username, password,
                aVoid -> {
                    assertEquals(username, sharedPreferences.getString(SharedPreferencesConstants.USERNAME, null));
                    assertTrue(sharedPreferences.getBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false));
                },
                e -> fail(e.getMessage()));

        authHelper.logoutUser();

        // Verify that the user session in SharedPreferences i
        assertNull(sharedPreferences.getString(SharedPreferencesConstants.USERNAME, null));
        assertFalse(sharedPreferences.getBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false));
    }
}
