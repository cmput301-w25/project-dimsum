package com.baobook.baobook;


import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.baobook.R;
import com.example.baobook.SignupActivity;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class SignupTest {
    @Rule
    public ActivityScenarioRule<SignupActivity> activityRule = new ActivityScenarioRule<>(SignupActivity.class);

    @Test
    public void SignupShouldShowErrorForEmptyCredentials() {
        // Simulate user clicking the login button with empty fields
        onView(withId(R.id.next_button)).perform(click());

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Please enter all fields")))
                .check(matches(isDisplayed()));
    }
}