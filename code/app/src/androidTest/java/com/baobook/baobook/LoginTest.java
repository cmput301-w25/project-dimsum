package com.baobook.baobook;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;


import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.baobook.LoginActivity;
import com.example.baobook.R;

import org.junit.Rule;
import org.junit.Test;

public class LoginTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginShouldShowErrorForEmptyCredentials() {
        // Simulate user clicking the login button with empty fields
        onView(withId(R.id.loginButton)).perform(click());

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Please enter all fields")))
                .check(matches(isDisplayed()));
    }
}
