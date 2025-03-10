package com.example.baobook.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Manager class for handling mood history globally.
 * Stores a list of MoodEvent, can sort them by date,
 * and provides a getFilteredList() method for all filtering logic.
 */
public class MoodHistoryManager {
    private static MoodHistoryManager instance;
    private ArrayList<MoodEvent> moodList;

    private MoodHistoryManager() {
        this.moodList = new ArrayList<>();
    }

    public static MoodHistoryManager getInstance() {
        if (instance == null) {
            instance = new MoodHistoryManager();
        }
        return instance;
    }

    // Add a single mood to the manager's list
    public void addMood(MoodEvent mood) {
        moodList.add(mood);
    }

    // Return the raw list (unfiltered)
    public ArrayList<MoodEvent> getMoodList() {
        return moodList;
    }

    // Clear all moods in the manager
    public void clearMoods() {
        moodList.clear();
    }

    // Sort the manager's list in reverse chronological order
    public void sortByDate() {
        Collections.sort(moodList, (m1, m2) -> {
            int dateComparison = m2.getDate().compareTo(m1.getDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return m2.getTime().compareTo(m1.getTime());
        });
    }

    // Existing single-mood filter (optional)
    public ArrayList<MoodEvent> filterByMood(Mood moodType) {
        ArrayList<MoodEvent> filteredList = new ArrayList<>();
        for (MoodEvent mood : moodList) {
            if (mood.getMood() == moodType) {
                filteredList.add(mood);
            }
        }
        return filteredList;
    }

    /**
     * Returns a filtered list of MoodEvent based on:
     * 1) Mood (if not null)
     * 2) Last 7 days (if true)
     * 3) Single-word partial match in description.
     * The word filter checks each token in the description.
     * Then sorts the result in descending date/time.
     */
    public ArrayList<MoodEvent> getFilteredList(Mood filterMood,
                                                boolean filterRecentWeek,
                                                String filterWord) {
        // Start with a copy of the entire list
        ArrayList<MoodEvent> temp = new ArrayList<>(moodList);

        // 1) Filter by mood (if not null)
        if (filterMood != null) {
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                if (me.getMood() != filterMood) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // 2) Filter by last 7 days
        if (filterRecentWeek) {
            long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                Date dateTime = combineDateAndTime(me.getDate(), me.getTime());
                if (dateTime.getTime() < oneWeekAgo) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // 3) Filter by single word in description (partial match on tokens)
        if (filterWord != null && !filterWord.trim().isEmpty()) {
            String lower = filterWord.toLowerCase();
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                String desc = (me.getDescription() == null) ? "" : me.getDescription().toLowerCase();
                // If none of the words in the description match partially, mark for removal
                if (!descriptionContainsWord(desc, lower)) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // Sort descending by date/time
        Collections.sort(temp, (m1, m2) -> {
            int dateComparison = m2.getDate().compareTo(m1.getDate());
            if (dateComparison != 0) return dateComparison;
            return m2.getTime().compareTo(m1.getTime());
        });

        return temp;
    }

    /**
     * Checks if the description contains the word.
     * It splits the description into tokens (words) and checks if any token:
     *  - Contains the filter word as a substring, OR
     *  - Has a Levenshtein distance <= 1 from the filter word.
     */
    private boolean descriptionContainsWord(String description, String word) {
        String[] tokens = description.split("\\s+");
        for (String token : tokens) {
            if (token.contains(word) || (levenshteinDistance(token, word) <= 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Standard dynamic programming approach to compute Levenshtein distance.
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        // Base case: transform empty string into prefix
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        // Fill the dp table
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,    // deletion
                                dp[i][j - 1] + 1),   // insertion
                        dp[i - 1][j - 1] + cost        // substitution
                );
            }
        }
        return dp[len1][len2];
    }

    /**
     * If your MoodEvent stores date and time separately,
     * combine them here. For now, simply return the date.
     */
    private Date combineDateAndTime(Date date, Date time) {
        return date;
    }
}
