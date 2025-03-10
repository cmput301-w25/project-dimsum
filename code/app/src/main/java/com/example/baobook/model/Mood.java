/**
 * Enum representing different types of moods.
 */

package com.example.baobook.model;

import androidx.annotation.NonNull;

/**
 * Enumeration of possible mood states.
 */
public enum Mood {
    HAPPINESS("Happiness"),
    SADNESS("Sadness"),
    DISGUST("Disgust"),
    FEAR("Fear"),

    SURPRISE("Surprise"),
    ANGER("Anger"),
    SHAME("Shame"),

    CONFUSION("Confusion");

    private final String prettyName;

    /**
     * Constructor for the Mood enum.
     *
     * @param prettyName Human-readable representation of the mood.
     */
    private Mood(String prettyName){
        this.prettyName = prettyName;
    }

    /**
     * Returns the human-readable name of the mood.
     *
     * @return Pretty name of the mood.
     */
    @NonNull
    @Override public String toString(){
        return prettyName;
    }


    /**
     * Parses a string to its corresponding Mood enum value.
     *
     * @param text The string representation of the mood.
     * @return The matching Mood enum value.
     * @throws IllegalArgumentException if the provided text doesn't match any Mood value.
     */
    public static Mood fromString(String text) {
        for (Mood m : Mood.values()) {
            if (m.prettyName.equalsIgnoreCase(text)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid Mood value: " + text);
    }
}
