package com.baobook.baobook;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.R;
import com.example.baobook.UserProfileActivity;
import com.example.baobook.util.UserSession;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {

    @Test
    public void testUsernameTextView() {
        // Create an intent to launch the activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserProfileActivity.class);
        intent.putExtra("username", "testuser"); // Pass the test username to the activity
        UserSession userSession = new UserSession(ApplicationProvider.getApplicationContext());
        userSession.setUsername("testuser");
        // Launch the activity
        try (ActivityScenario<UserProfileActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if the TextView displays "testuser"
            Espresso.onView(ViewMatchers.withId(R.id.username_text))
                    .check(ViewAssertions.matches(ViewMatchers.withText("testuser")));
        }
    }
}



