package com.baobook.baobook;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static java.util.EnumSet.allOf;
import static org.hamcrest.CoreMatchers.not;


import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.baobook.R;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodHistory;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
//import androidx.test.espresso.contrib.PickerActions;



public class MoodHistoryTest {
    @Rule
    public ActivityScenarioRule<MoodHistory> scenario = new ActivityScenarioRule<MoodHistory>(MoodHistory.class);

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @After
    public void clearDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("moodEvents");

        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (var document : task.getResult()) {
                    moodRef.document(document.getId()).delete()
                            .addOnFailureListener(e -> Log.e("Firestore", "Error deleting document", e));
                }
            } else {
                Log.e("Firestore", "Error fetching documents", task.getException());
            }
        });

        SystemClock.sleep(2000); // Optional: Wait for Firestore operations to complete
    }

    @Test
    public void AddMoodEvent() {
        // Click the add button
        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.edit_description)).perform(typeText("test"), pressImeActionButton());
        SystemClock.sleep(500);
        // Click the spinner to open dropdown
        onView(withId(R.id.mood_spinner)).perform(click());
        SystemClock.sleep(500);

        // Select the "Fear" option from the dropdown
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);


        onView(withId(R.id.clear_filter_button)).perform(click());
        SystemClock.sleep(500);
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.button_edit_mood)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.edit_mood_spinner)).check(matches(withSpinnerText(containsString("Happiness"))));
        SystemClock.sleep(500);
        onView(withId(R.id.edit_description)).check(matches(withText("test")));

    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("moodEvents");

        // Seed data with just two moods
        MoodEvent[] moods = {
                new MoodEvent("1", Mood.FEAR, new Date(), new Date(), "Bad", "Alone"),
                new MoodEvent("2", Mood.SADNESS, new Date(), new Date(), "Bad", "Alone")
        };

        for (MoodEvent mood : moods) {
            moodRef.add(mood)
                    .addOnSuccessListener(documentReference -> {
                        mood.setId(documentReference.getId()); // Set Firestore ID for each document
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error seeding mood", e);
                    });
        }

        SystemClock.sleep(2000); // Optional: give Firestore some time to process
    }

    @Test
    public void EditMoodEvent() {
        // Clear filters and select Fear
        onView(withId(R.id.clear_filter_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("😨 Fear")).perform(click());

        // Click edit mood button
        SystemClock.sleep(500);
        onView(withId(R.id.button_edit_mood)).perform(click());

        // Change mood to Happiness
        SystemClock.sleep(500);
        onView(withId(R.id.edit_mood_spinner)).perform(click());
        SystemClock.sleep(500);
        onView(withText("😊 Happiness"))
                .inRoot(isPlatformPopup())
                .perform(click());

        // Clear and update description
        onView(withId(R.id.edit_description)).perform(clearText(), typeText("test revamped"), closeSoftKeyboard());

        // Click save (Assuming it's a dialog, so using android.R.id.button1)
        SystemClock.sleep(500);
        onView(withId(android.R.id.button1)).perform(click());

        // Verify changes
        onView(withId(R.id.clear_filter_button)).perform(click());
        SystemClock.sleep(500);
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.button_edit_mood)).perform(click());
        SystemClock.sleep(500);

        // Check updated values
        onView(withId(R.id.edit_mood_spinner)).check(matches(withSpinnerText(containsString("Happiness"))));
        onView(withId(R.id.edit_description)).check(matches(withText("test revamped")));
    }

    @Test
    public void deleteMood() {
        onView(withId(R.id.clear_filter_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("😨 Fear")).perform(click());


        SystemClock.sleep(500);
        onView(withId(R.id.button_delete_mood)).perform(click());

        SystemClock.sleep(500);
        onView(withId(R.id.clear_filter_button)).perform(click());
        onView(withText("😨 Fear")).check(doesNotExist());

    }

    @Test
    public void AddError() {
        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.text_time)).perform(click());
        SystemClock.sleep(500);
        //Edit time for picker
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(setTime(23, 59));  // Custom method to set time

        // Confirm the time selection (click "OK" button)
        onView(withText("OK")).perform(click());
        SystemClock.sleep(500);
        onView(withText("Cannot select a future time"))
                .check(matches(isDisplayed()));

        SystemClock.sleep(500);
        onView(withId(R.id.text_date)).perform(click());
        SystemClock.sleep(500);

        // Set the date picker to December 31st, 2099 (far future)
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(setDate(2099, 12, 31));  // Custom method to set future date

        // Confirm date selection
        onView(withText("OK")).perform(click());

        SystemClock.sleep(500);
        onView(withText("Cannot select a future date"))
                .check(matches(isDisplayed()));





    }
    private static ViewAction setTime(final int hour, final int minute) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return Matchers.allOf(
                        withClassName(Matchers.equalTo(TimePicker.class.getName())),
                        ViewMatchers.isDisplayed()
                );
            }

            @Override
            public String getDescription() {
                return "set time on TimePicker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TimePicker timePicker = (TimePicker) view;
                timePicker.setCurrentHour(hour);  // Set the hour
                timePicker.setCurrentMinute(minute);  // Set the minute
            }
        };
    }

    public static ViewAction setDate(final int year, final int month, final int day) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DatePicker.class);
            }

            @Override
            public String getDescription() {
                return "Set the date to " + year + "-" + (month + 1) + "-" + day;
            }

            @Override
            public void perform(UiController uiController, View view) {
                DatePicker datePicker = (DatePicker) view;
                datePicker.updateDate(year, month, day);
            }
        };
    }


}
