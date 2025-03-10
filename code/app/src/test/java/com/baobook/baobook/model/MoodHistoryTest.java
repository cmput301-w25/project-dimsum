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

public class MoodHistoryTest {
    private MoodHistoryManager manager;

    @Before
    public void setUp() {
        manager = MoodHistoryManager.getInstance();
        manager.clearMoods(); // Clear any existing moods before each test
    }

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
        manager.addMood(new MoodEvent(Mood.HAPPINESS, date2, time2, "Middle"));
        manager.addMood(new MoodEvent(Mood.SADNESS, date3, time3, "Oldest"));
        manager.addMood(new MoodEvent(Mood.ANGER, date1, time1, "Most Recent"));

        // Sort the list
        manager.sortByDate();

        // Verify the order is most recent to oldest
        ArrayList<MoodEvent> sortedList = manager.getMoodList();
        assertEquals("Most Recent", sortedList.get(0).getDescription());
        assertEquals("Middle", sortedList.get(1).getDescription());
        assertEquals("Oldest", sortedList.get(2).getDescription());
    }

    @Test
    public void testSortMoodHistoryByDate_SameDate_DifferentTimes() {
        // Create test times for same date
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15);
        Date sameDate = cal.getTime();

        // Different times on same date
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        Time time1 = new Time(cal.getTimeInMillis());

        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        Time time2 = new Time(cal.getTimeInMillis());

        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 15);
        Time time3 = new Time(cal.getTimeInMillis());

        // Add moods in random order
        manager.addMood(new MoodEvent(Mood.HAPPINESS, sameDate, time2, "Noon"));
        manager.addMood(new MoodEvent(Mood.SADNESS, sameDate, time3, "Morning"));
        manager.addMood(new MoodEvent(Mood.ANGER, sameDate, time1, "Afternoon"));

        // Sort the list
        manager.sortByDate();

        // Verify the order is latest time to earliest
        ArrayList<MoodEvent> sortedList = manager.getMoodList();
        assertEquals("Afternoon", sortedList.get(0).getDescription());
        assertEquals("Noon", sortedList.get(1).getDescription());
        assertEquals("Morning", sortedList.get(2).getDescription());
    }

    @Test
    public void testFilterByMood_SingleMoodType() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        manager.addMood(new MoodEvent(Mood.HAPPINESS, date, time, "Happy1"));
        manager.addMood(new MoodEvent(Mood.SADNESS, date, time, "Sad"));
        manager.addMood(new MoodEvent(Mood.HAPPINESS, date, time, "Happy2"));
        manager.addMood(new MoodEvent(Mood.ANGER, date, time, "Angry"));
        manager.addMood(new MoodEvent(Mood.HAPPINESS, date, time, "Happy3"));

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

        manager.addMood(new MoodEvent(Mood.HAPPINESS, date, time, "Happy1"));
        manager.addMood(new MoodEvent(Mood.HAPPINESS, date, time, "Happy2"));

        // Filter for a different mood type
        ArrayList<MoodEvent> filteredList = manager.filterByMood(Mood.SADNESS);

        // Verify the filtered list is empty
        assertTrue(filteredList.isEmpty());
    }

    @Test
    public void testFilterByMood_EmptyList() {
        // Filter an empty list
        ArrayList<MoodEvent> filteredList = manager.filterByMood(Mood.HAPPINESS);

        // Verify the filtered list is empty
        assertTrue(filteredList.isEmpty());
    }
} 