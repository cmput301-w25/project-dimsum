package com.example.baobook.model;

import android.location.Location;

import com.example.baobook.controller.MoodHistoryManager;

import java.util.ArrayList;

public class MoodFilterState {
    private Mood mood = null;
    private boolean isRecentWeek = false;
    private boolean within5km = false;
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

    public ArrayList<MoodEvent> applyFilters(ArrayList<MoodEvent> moodEventsList, Location userLocation) {
        return MoodHistoryManager.getFilteredList(moodEventsList, mood, isRecentWeek, within5km, userLocation, word);
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

    public boolean isWithin5km() {
        return within5km;
    }

    public void setWithin5km(boolean isWithin5km) {
        this.within5km = isWithin5km;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
