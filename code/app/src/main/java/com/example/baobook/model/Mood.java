package com.example.baobook.model;

import androidx.annotation.NonNull;

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

    private Mood(String prettyName){
        this.prettyName = prettyName;
    }

    @NonNull
    @Override public String toString(){
        return prettyName;
    }

    public static Mood fromString(String text) {
        for (Mood m : Mood.values()) {
            if (m.prettyName.equalsIgnoreCase(text)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid Mood value: " + text);
    }
}
