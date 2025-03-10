package com.example.baobook;
/*
Created parallel lists to get the color and emoji for a certain mood
 */

import android.graphics.Color;

public class MoodUtils {

    // Define mood options
    public static final String[] MOOD_OPTIONS = {
            "Anger",
            "Confusion",
            "Sadness",
            "Disgust",
            "Fear",
            "Happiness",
            "Shame",
            "Surprise"
    };

    // Define colors for each mood
    public static final int[] MOOD_COLORS = {
            Color.parseColor("#FF0000"), // Anger (Red)
            Color.parseColor("#FFFF00"), // Confusion (Yellow)
            Color.parseColor("#0000FF"), // Sadness (Blue)
            Color.parseColor("#008000"), // Disgust (Green)
            Color.parseColor("#FF00FF"), // Fear (Magenta)
            Color.parseColor("#00FFFF"), // Happiness (Cyan)
            Color.parseColor("#808080"), // Shame (Gray)
            Color.parseColor("#D3D3D3")  // Surprise (Light Gray)
    };

    // Define emojis for each mood
    public static final String[] MOOD_EMOJIS = {
            "😠", // Anger
            "😕", // Confusion
            "😢", // Sadness
            "🤢", // Disgust
            "😨", // Fear
            "😊", // Happiness
            "😳", // Shame
            "😮"  // Surprise
    };

    // Helper method to get the index of a mood state
    public static int getMoodIndex(String moodState) {
        for (int i = 0; i < MOOD_OPTIONS.length; i++) {
            if (MOOD_OPTIONS[i].equals(moodState)) {
                return i;
            }
        }
        return 0; // Default to Anger if not found
    }

    // Helper method to get the color for a mood state
    public static int getMoodColor(String moodState) {
        int index = getMoodIndex(moodState);
        return MOOD_COLORS[index];
    }

    // Helper method to get the emoji for a mood state
    public static String getMoodEmoji(String moodState) {
        int index = getMoodIndex(moodState);
        return MOOD_EMOJIS[index];
    }
}