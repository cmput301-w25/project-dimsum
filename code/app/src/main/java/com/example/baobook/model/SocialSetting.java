package com.example.baobook.model;

import androidx.annotation.NonNull;

public enum SocialSetting {
    ALONE("Alone"),
    PAIR("Pair"),
    SMALL_GROUP("Small group"),
    CROWD("Crowd");

    private final String prettyName;

    SocialSetting(String prettyName) {
        this.prettyName = prettyName;
    }

    @NonNull
    @Override public String toString(){
        return prettyName;
    }

    public static SocialSetting fromString(String text) {
        for (SocialSetting ss : SocialSetting.values()) {
            if (ss.prettyName.equalsIgnoreCase(text)) {
                return ss;
            }
        }
        throw new IllegalArgumentException("Invalid Mood value: " + text);
    }
}
