/**
 * Manages mood history by storing, sorting, and filtering a list of MoodEvent objects.
 */

package com.example.baobook.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    /**
     * Retrieves the singleton instance of MoodHistoryManager.
     *
     * @return the single instance of MoodHistoryManager
     */
    public static MoodHistoryManager getInstance() {
        if (instance == null) {
            instance = new MoodHistoryManager();
        }
        return instance;
    }

    /**
     * Adds a single MoodEvent to the mood list.
     *
     * @param mood the MoodEvent to add
     */

    public void addMood(MoodEvent mood) {
        moodList.add(mood);
    }
    /**
     * Adds multiple MoodEvents to the mood list.
     *
     * @param moods a list of MoodEvents to add
     */
    public void addAllMoods(List<MoodEvent> moods) {
        moodList.addAll(moods);
    }

    /**
     * Retrieves the raw (unfiltered) mood list.
     *
     * @return the list of all MoodEvents
     */
    public ArrayList<MoodEvent> getMoodList() {
        return moodList;
    }

    // Clear all moods in the manager
    public void clearMoods() {
        moodList.clear();
    }

    // Sort the manager's list in reverse chronological order
    public void sortByDate() {
        moodList.sort(Comparator.comparing(MoodEvent::getDateTime).reversed());
    }

    /**
     * Filters MoodEvents by a specific Mood.
     *
     * @param moodType the mood to filter by
     * @return a filtered list of MoodEvents matching the moodType
     */
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
     * Filters the mood list based on mood type, recent week, and keyword in description.
     *
     * @param filterMood       the Mood to filter by (null to skip this filter)
     * @param filterRecentWeek true to filter only events from the last 7 days
     * @param filterWord       keyword for partial matching in descriptions (null or empty to skip)
     * @return a filtered and sorted list of MoodEvents
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
            OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                OffsetDateTime dateTime = me.getDateTime();
                if (dateTime.isBefore(oneWeekAgo)) {
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
                if (descriptionContainsWord(desc, lower)) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // Sort descending by date/time
        temp.sort(Comparator.comparing(MoodEvent::getDateTime).reversed());

        return temp;
    }

    public static ArrayList<MoodEvent> getFilteredList(ArrayList<MoodEvent> moodEvents,
                                                       Mood filterMood,
                                                       boolean filterRecentWeek,
                                                       String filterWord) {
        // Start with a copy of the entire list
        ArrayList<MoodEvent> temp = new ArrayList<>(moodEvents);

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
            OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);
            ArrayList<MoodEvent> toRemove = new ArrayList<>();
            for (MoodEvent me : temp) {
                OffsetDateTime dateTime = me.getDateTime();
                if (dateTime.isBefore(oneWeekAgo)) {
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
                if (descriptionContainsWord(desc, lower)) {
                    toRemove.add(me);
                }
            }
            temp.removeAll(toRemove);
        }

        // Sort descending by date/time
        temp.sort(Comparator.comparing(MoodEvent::getDateTime).reversed());

        return temp;
    }

    /**
     * Checks if a description contains the specified word using substring or Levenshtein distance.
     *
     * @param description the mood event description
     * @param word        the keyword to search for
     * @return true if the description matches criteria
     */
    private static boolean descriptionContainsWord(String description, String word) {
        String[] tokens = description.split("\\s+");
        for (String token : tokens) {
            if (token.contains(word) || (levenshteinDistance(token, word) <= 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * @param s1 first string
     * @param s2 second string
     * @return the Levenshtein distance
     */
    private static int levenshteinDistance(String s1, String s2) {
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
}
