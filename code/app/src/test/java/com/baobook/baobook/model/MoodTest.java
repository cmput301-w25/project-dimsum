package com.baobook.baobook.model;

import static org.junit.Assert.assertEquals;
import com.example.baobook.model.Mood;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class MoodTest {
    private final String prettyName;
    private final Mood mood;

    public MoodTest(String prettyName, Mood mood) {
        this.prettyName = prettyName;
        this.mood = mood;
    }

    @Test
    public void test_fromString() {
        assertEquals(mood, Mood.fromString(prettyName));
    }

    @Test
    public void test_fromString_invalidString() {
        String invalidMood = "invalid mood";
        try {
            Mood.fromString(invalidMood);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Invalid Mood value: " + invalidMood);
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
    }

    @Test
    public void test_toString_validString() {
        assertEquals(prettyName, mood.toString());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> prettyName_moodEnum() {
        return Arrays.asList(new Object[][] {
                { "Happiness", Mood.HAPPINESS },
                { "Sadness", Mood.SADNESS },
                { "Disgust", Mood.DISGUST },
                { "Fear", Mood.FEAR },
                { "Surprise", Mood.SURPRISE },
                { "Anger", Mood.ANGER },
                { "Shame", Mood.SHAME },
                { "Confusion", Mood.CONFUSION }

        });
    }
}