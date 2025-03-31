package com.baobook.baobook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.baobook.LoginActivity;
import com.example.baobook.R;
import com.example.baobook.UserProfileActivity;
import com.example.baobook.model.User;
import com.example.baobook.util.UserSession;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class UserLevelTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    @BeforeClass
    public static void setupClass() {
        // Set Firestore emulator address
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void cleanAndSeedDatabase() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userRef = db.collection("Users");

        // Clear existing users
        Task<QuerySnapshot> task = userRef.get();
        QuerySnapshot snapshot = Tasks.await(task);
        if (snapshot != null) {
            for (QueryDocumentSnapshot doc : snapshot) {
                Task<Void> deleteTask = userRef.document(doc.getId()).delete();
                Tasks.await(deleteTask);
            }
        }

        // Seed new users using username as document ID
        User[] users = {
                new User("alice", "1234", 1, 0, 10),
                new User("bob", "1234", 2, 0, 10)
        };

        for (User user : users) {
            Task<?> addTask = userRef.document(user.getUsername()).set(user);
            Tasks.await(addTask);
        }

        // Set alice as the "logged-in" user
        Intents.init();
        UserSession session = new UserSession(ApplicationProvider.getApplicationContext());
        session.setUsername("alice");
        session.setLevel(0);
        session.setExp(0);
        session.setExpNeeded(10);
    }

    @After
    public void tearDown() {
        try {
            Intents.release();
        } catch (IllegalStateException e) {
            Log.w("UserLevelTest", "Intents.release() called without init()");
        }
    }

    @Test
    public void testSaysExpGained() {

        onView(withId(R.id.usernameInput)).perform(typeText("alice"));
        SystemClock.sleep(500);
        onView(withId(R.id.passwordInput)).perform(typeText("1234"));
        SystemClock.sleep(500);
        onView(withId(R.id.loginButton)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.profile_button)).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);
        onView(withText("You gained 5 XP!"))
                .check(matches(isDisplayed()));

    }
    @Test
    public void testSaysLevelGained() {

        onView(withId(R.id.usernameInput)).perform(typeText("alice"));
        SystemClock.sleep(500);
        onView(withId(R.id.passwordInput)).perform(typeText("1234"));
        SystemClock.sleep(500);
        onView(withId(R.id.loginButton)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.profile_button)).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(5000);
        onView(withId(R.id.add_button)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.save_button)).perform(click());
        SystemClock.sleep(500);
        onView(withText("🎉 You leveled up to Level 2!"))
                .check(matches(isDisplayed()));
        SystemClock.sleep(5000);
        onView(withId(R.id.home_button)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.profile_button)).perform(click());
        SystemClock.sleep(500);



    }
}
