package com.baobook.baobook;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.R;
import com.example.baobook.UserProfileActivity;
import com.example.baobook.util.UserSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {
    private Intent intent;
    @Before
    public void setUp(){
        Intents.init();
        intent = new Intent(ApplicationProvider.getApplicationContext(), UserProfileActivity.class);
        UserSession userSession = new UserSession(ApplicationProvider.getApplicationContext());
        userSession.setUsername("testUser");

    }
    @After
    public void tearDown(){
        Intents.release();
    }
    @Test
    public void testUsernameTextView() {
        intent.putExtra("username", "testUser"); // Pass the test username to the activity
        ActivityScenario.launch(intent);
        // Launch the activity
        try (ActivityScenario<UserProfileActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if the TextView displays "testuser"
            Espresso.onView(ViewMatchers.withId(R.id.username_text))
                    .check(ViewAssertions.matches(ViewMatchers.withText("testuser")));
        }
    }
    @Test
    public void testShowOtherUsername() {
        intent.putExtra("userID", "otherUser"); // Pass the other username to the activity
        ActivityScenario.launch(intent);
        // Launch the activity
        try (ActivityScenario<UserProfileActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if the TextView displays "otheruser"
            Espresso.onView(ViewMatchers.withId(R.id.username_text))
                    .check(ViewAssertions.matches(ViewMatchers.withText("otheruser")));
        }
    }
    @Test
    public void testFollowButton() {
        intent.putExtra("userID", "otherUser"); // Pass the other username to the activity
        ActivityScenario.launch(intent);
        // Check if the follow button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        // Check if the follow button text is "Follow"
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
                .check(ViewAssertions.matches(ViewMatchers.withText("Follow")));
    }
    @Test
    public void testFollowButtonRequested() {
        intent.putExtra("userID", "otherUser"); // Pass the other username to the activity
        ActivityScenario.launch(intent);
        // Check if the follow button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        // Check if the follow button text is "Requested" when clicked
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
                .check(ViewAssertions.matches(ViewMatchers.withText("Requested")));
    }
    @Test
    public void testFollowButtonShouldNotShowOnUserProfile() {
        intent.putExtra("username", "testUser"); // Pass the test username to the activity
        ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }




}



