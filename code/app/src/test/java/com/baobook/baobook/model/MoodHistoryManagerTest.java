package com.baobook.baobook.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodHistoryManager;

import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Tests for the MoodHistoryManager's functionality, including
 * date sorting, mood filtering, and partial-word filtering.
 */
public class MoodHistoryManagerTest {

    private MoodHistoryManager manager;
    private static final String TEST_USERNAME = "testUser"; // For our mock MoodEvents

    @Before
    public void setUp() {
        // Get the singleton, and clear it before each test
        manager = MoodHistoryManager.getInstance();
        manager.clearMoods();
    }

    // ---------------------------------------------------------------------------------------------
    // Sorting Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testSortMoodHistoryByDate_ReverseChronological() {
        // Create test dates
        Calendar cal = Calendar.getInstance();

        // Create most recent date
        cal.set(2024, Calendar.MARCH, 15, 10, 0);
        Date date1 = cal.getTime();
        Time time1 = new Time(cal.getTimeInMillis());

        // Create middle date
        cal.set(2024, Calendar.MARCH, 14, 15, 30);
        Date date2 = cal.getTime();
        Time time2 = new Time(cal.getTimeInMillis());

        // Create oldest date
        cal.set(2024, Calendar.MARCH, 13, 9, 45);
        Date date3 = cal.getTime();
        Time time3 = new Time(cal.getTimeInMillis());

        // Add moods in random order
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date2, time2, "Middle"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.SADNESS, date3, time3, "Oldest"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.ANGER, date1, time1, "Most Recent"));

        // Sort the list
        manager.sortByDate();

        // Verify the order is [Most Recent, Middle, Oldest]
        ArrayList<MoodEvent> sortedList = manager.getMoodList();
        assertEquals("Most Recent", sortedList.get(0).getDescription());
        assertEquals("Middle", sortedList.get(1).getDescription());
        assertEquals("Oldest", sortedList.get(2).getDescription());
    }

    @Test
    public void testSortMoodHistoryByDate_SameDate_DifferentTimes() {
        // Create a single date
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15);
        Date sameDate = cal.getTime();

        // Different times on that date
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        Time time1 = new Time(cal.getTimeInMillis()); // 14:30

        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        Time time2 = new Time(cal.getTimeInMillis()); // 12:00

        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 15);
        Time time3 = new Time(cal.getTimeInMillis()); // 09:15

        // Add moods in random order
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, sameDate, time2, "Noon"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.SADNESS, sameDate, time3, "Morning"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.ANGER, sameDate, time1, "Afternoon"));

        // Sort the list
        manager.sortByDate();

        // Should be [Afternoon (14:30), Noon (12:00), Morning (09:15)]
        ArrayList<MoodEvent> sortedList = manager.getMoodList();
        assertEquals("Afternoon", sortedList.get(0).getDescription());
        assertEquals("Noon", sortedList.get(1).getDescription());
        assertEquals("Morning", sortedList.get(2).getDescription());
    }

    // ---------------------------------------------------------------------------------------------
    // filterByMood(...) Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testFilterByMood_SingleMoodType() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "Happy1"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.SADNESS, date, time, "Sad"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "Happy2"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.ANGER, date, time, "Angry"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "Happy3"));

        // Filter for happy moods
        ArrayList<MoodEvent> filteredList = manager.filterByMood(Mood.HAPPINESS);

        // Verify only happy moods are returned
        assertEquals(3, filteredList.size());
        for (MoodEvent mood : filteredList) {
            assertEquals(Mood.HAPPINESS, mood.getMood());
        }
    }

    @Test
    public void testFilterByMood_NoMatchingMoods() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "Happy1"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "Happy2"));

        // Filter for a different mood type
        ArrayList<MoodEvent> filteredList = manager.filterByMood(Mood.SADNESS);

        // Verify the filtered list is empty
        assertTrue(filteredList.isEmpty());
    }

    @Test
    public void testFilterByMood_EmptyList() {
        // Filter an empty manager
        ArrayList<MoodEvent> filteredList = manager.filterByMood(Mood.HAPPINESS);
        assertTrue(filteredList.isEmpty());
    }

    // ---------------------------------------------------------------------------------------------
    // getFilteredList(...) Word (partial-match) Tests
    // We pass (null, false, <word>) to skip mood/time and only do word filter.
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testFilterByWord_PartialMatch() {
        // This checks partial substring matches for "hel"
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "hello world"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.SADNESS, date, time, "helipad stuff"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.FEAR, date, time, "my best friend"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.DISGUST, date, time, "Help me!"));

        // We'll not filter by mood or last7days => pass (null, false, "hel")
        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "hel");

        // We expect 3 matches: "hello world", "helipad stuff", and "Help me!"
        // "my best friend" does not match
        assertEquals("Expected 3 matches containing 'hel'", 3, filtered.size());

        // (Optional) Check each description
        for (MoodEvent me : filtered) {
            String descLower = me.getDescription().toLowerCase();
            assertTrue("Description should contain 'hel': " + me.getDescription(),
                    descLower.contains("hel"));
        }
    }

    @Test
    public void testFilterByWord_NoPartialMatch() {
        // If the filter word doesn't appear at all => empty result
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "hello world"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.SADNESS, date, time, "helipad stuff"));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "xyz");
        assertTrue("Expecting no matches for 'xyz'", filtered.isEmpty());
    }

    @Test
    public void testFilterByWord_CaseInsensitivePartialMatch() {
        // If your code does case-insensitive matching, verify:
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "Hello World"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.SADNESS, date, time, "HELLO AGAIN"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.FEAR, date, time, "HeLium balloon"));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "hel");

        // All 3 match ignoring case
        assertEquals("All 3 match ignoring case", 3, filtered.size());
    }

    @Test
    public void testFilterByWord_OneEditAway() {

        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());
        // User typed "helo," we want to match "hello."
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "hello friend"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "some other desc"));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "helo");
        assertEquals("Expect 'hello friend' to match 'helo' by 1 edit distance", 1, filtered.size());
        assertEquals("hello friend", filtered.get(0).getDescription());
    }

    @Test
    public void testFilterByWord_EmptyString() {
        // If user enters empty => no word filtering => all remain
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.ANGER, date, time, "hello test"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "something"));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "");
        assertEquals("Empty filter means no filtering, so all remain", 2, filtered.size());
    }

    // ---------------------------------------------------------------------------------------------
    // (Optional) Combined Filters Tests
    // E.g. mood + last 7 days + partial word
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testGetFilteredList_Combined() {
        // We'll test a scenario with a mood = HAPPINESS, lastWeek = false, and word = "hello"
        // Add data, then see if we get the correct subset
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "hello world"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "random text"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.ANGER, date, time, "hello friend"));
        manager.addMood(new MoodEvent(TEST_USERNAME, Mood.HAPPINESS, date, time, "HELLO AGAIN"));

        // Filter: Mood=HAPPINESS, lastWeek=false, word="hello"
        ArrayList<MoodEvent> filtered = manager.getFilteredList(Mood.HAPPINESS, false, "hello");

        // "hello world" => mood=HAPPINESS, desc contains "hello"
        // "random text" => mood=HAPPINESS but no "hello" => excluded
        // "hello friend" => has "hello" but mood=ANGER => excluded
        // "HELLO AGAIN" => mood=HAPPINESS, partial match "HELLO" => included
        // => We expect 2 results
        assertEquals("Expecting 2 results for mood=HAPPINESS & word='hello'", 2, filtered.size());

        // Check them specifically
        ArrayList<String> descs = new ArrayList<>();
        for (MoodEvent me : filtered) {
            descs.add(me.getDescription());
        }
        assertTrue(descs.contains("hello world"));
        assertTrue(descs.contains("HELLO AGAIN"));
    }
}
