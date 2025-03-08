package com.baobook.baobook.model;

import static org.junit.Assert.assertEquals;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.SocialSetting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.sql.Time;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@RunWith(JUnitParamsRunner.class)
public class MoodEventTest {

    private static final String username = "username";
    private static final Mood mood = Mood.HAPPINESS;
    private static final Date date = new Date("July 20, 2012");
    private static final Time time = Time.valueOf("00:00:00");
    private static final String description = "just ate a pizza pop";


    @Test
    @Parameters(method="getMoods")
    public void getMood_shouldReturnExpectedMood(Mood mood) {
        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(mood, cut.getMood());
    }

    @Test
    @Parameters(method="getMoodTransitions")
    public void setMood_shouldChangeMood(Mood mood, Mood newMood) {
        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(mood, cut.getMood());

        cut.setMood(newMood);
        assertEquals(newMood, cut.getMood());
    }

    @Test
    public void getDate_shouldReturnExpectedDate() {
        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(date, cut.getDate());
    }

    @Test
    public void setDate_shouldChangeDate() {
        Date newDate = new Date("March 4, 2025");

        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(date, cut.getDate());

        cut.setDate(newDate);
        assertEquals(newDate, cut.getDate());
    }

    @Test
    public void getTime_shouldReturnExpectedTime() {
        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(time, cut.getTime());
    }

    @Test
    public void setTime_shouldChangeTime() {
        Time newTime = Time.valueOf("12:34:56");

        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(time, cut.getTime());

        cut.setTime(newTime);
        assertEquals(newTime, cut.getTime());
    }

    @Test
    public void getDescription_shouldReturnExpectedDescription() {
        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(description, cut.getDescription());
    }

    @Test
    public void setDescription_shouldChangeDescription() {
        String newDescription = "i'm hungry";

        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(description, cut.getDescription());

        cut.setDescription(newDescription);
        assertEquals(newDescription, cut.getDescription());
    }

    @Test
    public void editMoodEvent_shouldChangeAllAttributes() {
        MoodEvent cut = new MoodEvent(username, mood, date, time, description);
        assertEquals(mood, cut.getMood());
        assertEquals(date, cut.getDate());
        assertEquals(time, cut.getTime());
        assertEquals(description, cut.getDescription());

        Mood newMood = Mood.ANGER;
        Date newDate = new Date("March 4, 2025");
        Time newTime = Time.valueOf("12:34:56");
        String newDescription = "i'm hungry";

        cut.editMoodEvent(newMood, newDate, newTime, newDescription);
        assertEquals(newMood, cut.getMood());
        assertEquals(newDate, cut.getDate());
        assertEquals(newTime, cut.getTime());
        assertEquals(newDescription, cut.getDescription());
    }

    public Collection<Mood> getMoods() {
        return Arrays.asList(
                Mood.HAPPINESS,
                Mood.SADNESS,
                Mood.SURPRISE,
                Mood.CONFUSION,
                Mood.FEAR,
                Mood.ANGER,
                Mood.DISGUST
        );
    }

    public Collection<Object[]> getMoodTransitions() {
        return Arrays.asList(new Object[][]{
                { Mood.HAPPINESS, Mood.SADNESS },
                { Mood.SADNESS, Mood.DISGUST },
                { Mood.DISGUST, Mood.FEAR },
                { Mood.FEAR, Mood.SURPRISE },
                { Mood.SURPRISE, Mood.ANGER },
                { Mood.ANGER, Mood.SHAME },
                { Mood.SHAME, Mood.CONFUSION },
                { Mood.CONFUSION, Mood.HAPPINESS }
        });
    }
}
