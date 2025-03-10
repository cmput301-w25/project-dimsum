/**
 * Represents a user with a username, password, and relationships such as followers and followings.
 */
package com.example.baobook.model;

import androidx.annotation.NonNull;
/**
 * Enum representing various social settings that a user might experience.
 */
public enum SocialSetting {
    ALONE("Alone"),
    PAIR("Pair"),
    SMALL_GROUP("Small group"),
    CROWD("Crowd");

    private final String prettyName;
    /**
     * Constructor for SocialSetting.
     * @param prettyName user-friendly name for the social setting
     */
    SocialSetting(String prettyName) {
        this.prettyName = prettyName;
    }

    /**
     * Returns a user-friendly name for the social setting.
     * @return user-friendly social setting name
     */
    @NonNull
    @Override public String toString(){
        return prettyName;
    }

    /**
     * Parses a string to its corresponding SocialSetting enum value.
     * @param text the string representation of the social setting
     * @return the matching SocialSetting enum value
     * @throws IllegalArgumentException if the provided text doesn't match any SocialSetting
     */
    public static SocialSetting fromString(String text) {
        for (SocialSetting ss : SocialSetting.values()) {
            if (ss.prettyName.equalsIgnoreCase(text)) {
                return ss;
            }
        }
        throw new IllegalArgumentException("Invalid Mood value: " + text);
    }
}
