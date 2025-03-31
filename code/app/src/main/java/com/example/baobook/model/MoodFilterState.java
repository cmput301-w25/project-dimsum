package com.example.baobook.model;

import android.location.Location;

import com.example.baobook.controller.MoodHistoryManager;

import java.util.ArrayList;


/**
 * Represents the state of filters applied to a list of {@link MoodEvent}s.
 * Allows filtering based on mood type, recency (within the last week),
 * proximity (within 5 km), and keyword in the description.
 */
public class MoodFilterState {
    private Mood mood = null;
    private boolean isRecentWeek = false;
    private boolean within5km = false;
    private String word = null;

    /**
     * Clears all filters by resetting them to their default values.
     */

    public void clear() {
        mood = null;
        isRecentWeek = false;
        word = null;
    }

    /**
     * Applies the current filters to the full mood event history from the manager.
     *
     * @return a filtered list of {@link MoodEvent}s
     */

    public ArrayList<MoodEvent> applyFilters() {
        return MoodHistoryManager.getInstance()
                .getFilteredList(mood, isRecentWeek, word);
    }

    /**
     * Applies the current filters to a given list of mood events.
     *
     * @param moodEventsList the list of mood events to filter
     * @return a filtered list of {@link MoodEvent}s
     */

    public ArrayList<MoodEvent> applyFilters(ArrayList<MoodEvent> moodEventsList) {
        return MoodHistoryManager.getFilteredList(moodEventsList, mood, isRecentWeek, word);
    }

    /**
     * Applies the current filters to a given list of mood events,
     * including filtering by distance from the user's location.
     *
     * @param moodEventsList the list of mood events to filter
     * @param userLocation   the user's current location
     * @return a filtered list of {@link MoodEvent}s
     */
    public ArrayList<MoodEvent> applyFilters(ArrayList<MoodEvent> moodEventsList, Location userLocation) {
        return MoodHistoryManager.getFilteredList(moodEventsList, mood, isRecentWeek, within5km, userLocation, word);
    }
    /**
     * Gets the selected mood filter.
     *
     * @return the mood filter
     */

    public Mood getMood() {
        return mood;
    }
    /**
     * Sets the mood filter.
     *
     * @param mood the mood to filter by
     */

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    /**
     * Checks if the "recent week" filter is enabled.
     *
     * @return true if only recent week events should be shown
     */

    public boolean isRecentWeek() {
        return isRecentWeek;
    }

    /**
     * Enables or disables the "recent week" filter.
     *
     * @param isRecentWeek true to filter only recent events
     */
    public void setRecentWeek(boolean isRecentWeek) {
        this.isRecentWeek = isRecentWeek;
    }

    /**
     * Checks if the "within 5 km" distance filter is enabled.
     *
     * @return true if filtering by proximity is enabled
     */
    public boolean isWithin5km() {
        return within5km;
    }

    /**
     * Enables or disables the "within 5 km" distance filter.
     *
     * @param isWithin5km true to filter only nearby events
     */
    public void setWithin5km(boolean isWithin5km) {
        this.within5km = isWithin5km;
    }

    /**
     * Gets the keyword used in the description filter.
     *
     * @return the keyword to match in mood event descriptions
     */
    public String getWord() {
        return word;
    }
    /**
     * Sets the keyword for filtering mood event descriptions.
     *
     * @param word the keyword to use
     */

    public void setWord(String word) {
        this.word = word;
    }
}
