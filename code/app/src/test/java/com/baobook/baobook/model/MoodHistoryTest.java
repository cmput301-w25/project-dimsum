package com.baobook.baobook.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodHistory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@RunWith(Parameterized.class)
public class MoodHistoryTest {
    private MoodHistory moodHistory;
    private ArrayList<MoodEvent> testDataList;

    @Before
    public void setUp() {
        moodHistory = new MoodHistory();
        testDataList = MoodHistory.getDataList();
        testDataList.clear();
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
        testDataList.add(new MoodEvent(Mood.HAPPINESS, date2, time2, "Middle"));
        testDataList.add(new MoodEvent(Mood.SADNESS, date3, time3, "Oldest"));
        testDataList.add(new MoodEvent(Mood.ANGER, date1, time1, "Most Recent"));

        // Verify the order is most recent to oldest
        assertEquals("Most Recent", testDataList.get(0).getDescription());
        assertEquals("Middle", testDataList.get(1).getDescription());
        assertEquals("Oldest", testDataList.get(2).getDescription());
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
        testDataList.add(new MoodEvent(Mood.HAPPINESS, sameDate, time2, "Noon"));
        testDataList.add(new MoodEvent(Mood.SADNESS, sameDate, time3, "Morning"));
        testDataList.add(new MoodEvent(Mood.ANGER, sameDate, time1, "Afternoon"));

        // Verify the order is latest time to earliest
        assertEquals("Afternoon", testDataList.get(0).getDescription());
        assertEquals("Noon", testDataList.get(1).getDescription());
        assertEquals("Morning", testDataList.get(2).getDescription());
    }

    @Test
    public void testFilterByMood_SingleMoodType() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        testDataList.add(new MoodEvent(Mood.HAPPINESS, date, time, "Happy1"));
        testDataList.add(new MoodEvent(Mood.SADNESS, date, time, "Sad"));
        testDataList.add(new MoodEvent(Mood.HAPPINESS, date, time, "Happy2"));
        testDataList.add(new MoodEvent(Mood.ANGER, date, time, "Angry"));
        testDataList.add(new MoodEvent(Mood.HAPPINESS, date, time, "Happy3"));

        // Filter for happy moods
        ArrayList<MoodEvent> filteredList = moodHistory.filterByMood(Mood.HAPPINESS);

        // Verify only happy moods are returned
        assertEquals(3, filteredList.size());
        for (MoodEvent mood : filteredList) {
            assertEquals(Mood.HAPPINESS, mood.getMood());
        }
    }

    @Test
    public void testFilterByMood_NoMatchingMoods() {
        // Add moods of only one type
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Time time = new Time(cal.getTimeInMillis());

        testDataList.add(new MoodEvent(Mood.HAPPINESS, date, time, "Happy1"));
        testDataList.add(new MoodEvent(Mood.HAPPINESS, date, time, "Happy2"));

        // Filter for a different mood type
        ArrayList<MoodEvent> filteredList = moodHistory.filterByMood(Mood.SADNESS);

        // Verify the filtered list is empty
        assertTrue(filteredList.isEmpty());
    }

    @Test
    public void testFilterByMood_EmptyList() {
        // Filter an empty list
        ArrayList<MoodEvent> filteredList = moodHistory.filterByMood(Mood.HAPPINESS);

        // Verify the filtered list is empty
        assertTrue(filteredList.isEmpty());
    }
} 