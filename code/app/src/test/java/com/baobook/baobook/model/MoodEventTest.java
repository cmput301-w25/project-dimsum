package com.baobook.baobook.model;

import static org.junit.Assert.assertEquals;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.SocialSetting;

import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;

@RunWith(JUnitParamsRunner.class)
public class MoodEventTest {
    private static final String id = "0";
    private static final String username = "username";
    private static final Mood mood = Mood.HAPPINESS;
    private static final OffsetDateTime dateTime = OffsetDateTime.parse("2012-07-20T12:00:00Z");
    private static final String description = "just ate a pizza pop";
    private static final SocialSetting socialSetting = SocialSetting.ALONE;

    @Test
    @Parameters(method="getMoods")
    public void getMood_shouldReturnExpectedMood(Mood mood) {
        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(mood, cut.getMood());
    }

    @Test
    @Parameters(method="getMoodTransitions")
    public void setMood_shouldChangeMood(Mood mood, Mood newMood) {
        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(mood, cut.getMood());

        cut.setMood(newMood);
        assertEquals(newMood, cut.getMood());
    }

    @Test
    public void getTimestamp_shouldReturnExpectedTimestamp() {
        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(dateTime, cut.getDateTime());
    }

    @Test
    public void setTimestamp_shouldChangeTimestamp() {
        OffsetDateTime newTimestamp = OffsetDateTime.parse("2025-03-04T14:30:00Z");

        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(dateTime, cut.getDateTime());

        cut.setDateTime(newTimestamp);
        assertEquals(newTimestamp, cut.getDateTime());
    }

    @Test
    public void getDescription_shouldReturnExpectedDescription() {
        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(description, cut.getDescription());
    }

    @Test
    public void setDescription_shouldChangeDescription() {
        String newDescription = "i'm hungry";

        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(description, cut.getDescription());

        cut.setDescription(newDescription);
        assertEquals(newDescription, cut.getDescription());
    }

    @Test
    public void editMoodEvent_shouldChangeAllAttributes() {
        MoodEvent cut = new MoodEvent(username, id, mood, dateTime, description, socialSetting, "");
        assertEquals(mood, cut.getMood());
        assertEquals(dateTime, cut.getDateTime());
        assertEquals(description, cut.getDescription());

        Mood newMood = Mood.ANGER;
        OffsetDateTime newTimestamp = OffsetDateTime.parse("2025-03-04T14:30:00Z");
        String newDescription = "i'm hungry";
        SocialSetting newSocial = SocialSetting.DUO;

        cut.editMoodEvent(newMood, newTimestamp, newDescription, newSocial);
        assertEquals(newMood, cut.getMood());
        assertEquals(newTimestamp, cut.getDateTime());
        assertEquals(newDescription, cut.getDescription());
        assertEquals(newSocial, cut.getSocial());
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
