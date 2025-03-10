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
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import androidx.test.espresso.intent.Intents;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;

public class MoodHistoryTest {
    private static final SocialSetting socialSetting = SocialSetting.ALONE;
    private static final OffsetDateTime date = OffsetDateTime.of(2025, 3, 10, 1, 1, 1, 1, ZoneOffset.UTC);

    @Rule
    public ActivityScenarioRule<MoodHistory> scenario = new ActivityScenarioRule<>(MoodHistory.class);

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void setUp() {
        try {
            Intents.init(); // ✅ Initialize Espresso Intents before each test
        } catch (IllegalStateException e) {
            Log.w("EspressoTest", "Intents.init() was already initialized.");
        }
    }

    @After
    public void tearDown() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("moodEvents");

        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    moodRef.document(document.getId()).delete()
                            .addOnFailureListener(e -> Log.e("Firestore", "Error deleting document", e));
                }
            } else {
                Log.e("Firestore", "Error fetching documents", task.getException());
            }
        });

        try {
            Intents.release(); // ✅ Safely release Intents after each test
        } catch (IllegalStateException e) {
            Log.w("EspressoTest", "Intents.release() called without init()");
        }
        SystemClock.sleep(2000);
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("moodEvents");

        MoodEvent[] moods = {
                new MoodEvent("idk", "1", Mood.FEAR, date, "Bad", socialSetting, ""),
                new MoodEvent("idk", "2", Mood.SADNESS, date, "Bad", socialSetting, "")
        };

        for (MoodEvent mood : moods) {
            moodRef.add(mood)
                    .addOnSuccessListener(documentReference -> mood.setId(documentReference.getId()))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error seeding mood", e));
        }

        SystemClock.sleep(2000);
    }

    @Test
    public void AddMoodEvent() {
        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);

        TakePhoto();
        SystemClock.sleep(500);

        onView(withId(R.id.edit_description)).perform(typeText("test"), pressImeActionButton());
        SystemClock.sleep(500);

        onView(withId(R.id.mood_spinner)).perform(click());
        SystemClock.sleep(500);

        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.clear_all_button)).perform(click());
        SystemClock.sleep(500);
        onView(withText("😊 Happiness")).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.button_edit_mood)).perform(click());

        onView(withId(R.id.edit_mood_spinner)).check(matches(withSpinnerText(containsString("Happiness"))));
        onView(withId(R.id.edit_description)).check(matches(withText("test")));
    }

    @Test
    public void deleteMood() {
        onView(withId(R.id.clear_all_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("😨 Fear")).perform(click());

        onView(withId(R.id.button_delete_mood)).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.clear_all_button)).perform(click());
        onView(withText("😨 Fear")).check(doesNotExist());
    }

    private void TakePhoto() {
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, createFakeImageResult()));

        onView(withId(R.id.openCamera)).perform(click());
        SystemClock.sleep(1000);
    }

    private Intent createFakeImageResult() {
        Intent resultData = new Intent();
        Bitmap fakeBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", fakeBitmap);
        resultData.putExtras(bundle);
        return resultData;
    }
}
