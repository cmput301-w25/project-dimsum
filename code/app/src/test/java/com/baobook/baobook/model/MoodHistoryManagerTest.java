package com.baobook.baobook.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodHistoryManager;
import com.example.baobook.model.SocialSetting;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Tests for the MoodHistoryManager's functionality, including
 * date sorting, mood filtering, and partial-word filtering.
 */
public class MoodHistoryManagerTest {

    private MoodHistoryManager manager;
    private static final String TEST_USERNAME = "testUser"; // For our mock MoodEvents
    private static final SocialSetting socialSetting = SocialSetting.ALONE;
    private static final OffsetDateTime date = LocalDateTime.now().atOffset(ZoneOffset.UTC);

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

        // Create most recent date
        OffsetDateTime date1 = OffsetDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC);

        // Create middle date
        OffsetDateTime date2 = OffsetDateTime.of(2024, 3, 14, 15, 30, 0, 0, ZoneOffset.UTC);

        // Create oldest date
        OffsetDateTime date3 = OffsetDateTime.of(2024, 3, 13, 9, 45, 0, 0, ZoneOffset.UTC);

        // Add moods in random order
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.HAPPINESS, date2, "Middle", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.SADNESS, date3, "Oldest", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "3", Mood.ANGER, date1, "Most Recent", socialSetting, ""));

        // Sort the list
        manager.sortByDate();

        // Verify the order is [Most Recent, Middle, Oldest]
        ArrayList<MoodEvent> sortedList = manager.getMoodList();
        assertEquals("Most Recent", sortedList.get(0).getDescription());
        assertEquals("Middle", sortedList.get(1).getDescription());
        assertEquals("Oldest", sortedList.get(2).getDescription());
    }

    // ---------------------------------------------------------------------------------------------
    // filterByMood(...) Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testFilterByMood_SingleMoodType() {
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "Happy1", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.SADNESS, date, "Sad", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "3", Mood.HAPPINESS, date, "Happy2", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "4", Mood.ANGER, date, "Angry", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "5", Mood.HAPPINESS, date, "Happy3", socialSetting, ""));

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
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "Happy1", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.HAPPINESS, date, "Happy2", socialSetting, ""));

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
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "hello world", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.SADNESS, date, "helipad stuff", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "3", Mood.FEAR, date, "my best friend", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "4", Mood.DISGUST, date, "Help me!", socialSetting, ""));

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
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "hello world", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.SADNESS, date, "helipad stuff", socialSetting, ""));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "xyz");
        assertTrue("Expecting no matches for 'xyz'", filtered.isEmpty());
    }

    @Test
    public void testFilterByWord_CaseInsensitivePartialMatch() {
        // If your code does case-insensitive matching, verify:
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "Hello World", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.SADNESS, date, "HELLO AGAIN", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "3", Mood.FEAR, date, "HeLium balloon", socialSetting, ""));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "hel");

        // All 3 match ignoring case
        assertEquals("All 3 match ignoring case", 3, filtered.size());
    }


    @Test
    public void testFilterByWord_OneEditAway() {
        Calendar cal = Calendar.getInstance();
        // User typed "helo," we want to match "hello."
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "hello friend", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.HAPPINESS, date, "some other desc", socialSetting, ""));

        ArrayList<MoodEvent> filtered = manager.getFilteredList(null, false, "helo");
        assertEquals("Expect 'hello friend' to match 'helo' by 1 edit distance", 1, filtered.size());
        assertEquals("hello friend", filtered.get(0).getDescription());
    }

    @Test
    public void testFilterByWord_EmptyString() {
        // If user enters empty => no word filtering => all remain
        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.ANGER, date, "hello test", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.HAPPINESS, date, "something", socialSetting, ""));

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

        manager.addMood(new MoodEvent(TEST_USERNAME, "1", Mood.HAPPINESS, date, "hello world", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "2", Mood.HAPPINESS, date, "random text", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "3", Mood.ANGER, date, "hello friend", socialSetting, ""));
        manager.addMood(new MoodEvent(TEST_USERNAME, "4", Mood.HAPPINESS, date, "HELLO AGAIN", socialSetting, ""));


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
