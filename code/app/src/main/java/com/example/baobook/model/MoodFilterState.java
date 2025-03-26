package com.example.baobook.model;

import java.util.ArrayList;

public class MoodFilterState {
    private Mood mood = null;
    private boolean isRecentWeek = false;
    private String word = null;

    public void clear() {
        mood = null;
        isRecentWeek = false;
        word = null;
    }

    public ArrayList<MoodEvent> applyFilters() {
        return MoodHistoryManager.getInstance()
                .getFilteredList(mood, isRecentWeek, word);
    }

    public ArrayList<MoodEvent> applyFilters(ArrayList<MoodEvent> moodEventsList) {
        return MoodHistoryManager.getFilteredList(moodEventsList, mood, isRecentWeek, word);
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public boolean isRecentWeek() {
        return isRecentWeek;
    }

    public void setRecentWeek(boolean isRecentWeek) {
        this.isRecentWeek = isRecentWeek;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
