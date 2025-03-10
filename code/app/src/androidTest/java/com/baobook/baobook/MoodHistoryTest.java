package com.baobook.baobook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import androidx.test.rule.GrantPermissionRule;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.example.baobook.R;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.MoodHistory;
import com.example.baobook.model.SocialSetting;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import androidx.test.espresso.intent.Intents;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;

public class MoodHistoryTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA);

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
    public void clearDatabase() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("MoodEvents");

        Task<QuerySnapshot> task = moodRef.get();
        QuerySnapshot snapshot = Tasks.await(task); // Wait for completion

        if (snapshot != null) {
            for (QueryDocumentSnapshot document : snapshot) {
                Task<Void> deleteTask = moodRef.document(document.getId()).delete();
                Tasks.await(deleteTask); // Wait for each deletion
            }
        }

        // Release Intents safely
        try {
            Intents.release();
        } catch (IllegalStateException e) {
            Log.w("EspressoTest", "Intents.release() called without init()");
        }

        SystemClock.sleep(2000); // Optional delay
    }


    @Test
    public void AddMoodEvent() {
        Intents.init();
        // Click the add mood button
        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);

        // Click the camera button to take a photo
        TakePhoto();
        SystemClock.sleep(500);

        // Enter description
        onView(withId(R.id.edit_description)).perform(typeText("test"), pressImeActionButton());
        SystemClock.sleep(500);

        // Click the mood spinner to open dropdown
        onView(withId(R.id.mood_spinner)).perform(click());
        SystemClock.sleep(500);

        // Select the "Happiness" mood
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);

        // Click save button
        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);

        // Verify the mood was added
        onView(withId(R.id.clear_all_button)).perform(click());
        SystemClock.sleep(500);
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.button_edit_mood)).perform(click());
        SystemClock.sleep(500);

        // Check if mood is saved correctly
        onView(withId(R.id.edit_mood_spinner)).check(matches(withSpinnerText(containsString("Happiness"))));
        SystemClock.sleep(500);
        onView(withId(R.id.edit_description)).check(matches(withText("test")));
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("MoodEvents");

        // Define a sample social setting
        SocialSetting socialSetting = SocialSetting.ALONE; // Ensure this is a valid enum or object

        // Seed data with just two moods
        MoodEvent[] moods = {
                new MoodEvent("idk", "1", Mood.FEAR, OffsetDateTime.now(), "Bad", socialSetting, ""),
                new MoodEvent("idk", "2", Mood.SADNESS, OffsetDateTime.now(), "Bad", socialSetting, "")
        };

        for (MoodEvent mood : moods) {
            moodRef.add(mood)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Seeded MoodEvent with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error seeding mood", e);
                    });
        }

        SystemClock.sleep(2000); // Optional: give Firestore some time to process
    }

    @Test
    public void EditMoodEvent() {
        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);

        // Enter description
        onView(withId(R.id.edit_description)).perform(typeText("test"), pressImeActionButton());
        SystemClock.sleep(500);

        // Click the mood spinner to open dropdown
        onView(withId(R.id.mood_spinner)).perform(click());
        SystemClock.sleep(500);

        // Select the "Fear" mood
        onView(withText("😨 Fear")).perform(click());
        SystemClock.sleep(500);

        // Click save button
        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);
        // Clear filters and select Fear
        onView(withId(R.id.clear_all_button)).perform(click());
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
        onView(withId(R.id.clear_all_button)).perform(click());
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

        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);

        // Enter description
        onView(withId(R.id.edit_description)).perform(typeText("test"), pressImeActionButton());
        SystemClock.sleep(500);

        // Click the mood spinner to open dropdown
        onView(withId(R.id.mood_spinner)).perform(click());
        SystemClock.sleep(500);

        // Select the "Happiness" mood
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);

        // Click save button
        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);


        onView(withId(R.id.clear_all_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("😊 Happiness")).perform(click());

        SystemClock.sleep(500);
        onView(withId(R.id.button_delete_mood)).perform(click());

        SystemClock.sleep(500);
        onView(withId(R.id.clear_all_button)).perform(click());
        onView(withText("😊 Happiness")).check(doesNotExist());
    }

    // ✅ Add TakePhoto() function here, after deleteMood()
    private void TakePhoto() {

        // Click the camera button
        //onView(withId(R.id.openCamera)).perform(click());
        //SystemClock.sleep(1000);

        // Mock camera response with a fake image, because espresso can't actually take photos on camera, so it'll jsut replace with black box
        Intent resultData = new Intent();
        Bitmap fakeBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", fakeBitmap);
        resultData.putExtras(bundle);

        // ✅ Simulate returning an image from the camera
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData));

        // Wait for image to be set in ImageView
        SystemClock.sleep(1000);
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
        onView(withText("Cannot select a time from the future"))
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
        onView(withText("Cannot select a date from the future"))
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
