package com.example.baobook.model;

import java.util.ArrayList;
import java.util.Collections;

//Manager class for handling mood history

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

    public void addMood(MoodEvent mood) {
        moodList.add(mood);
    }

    public ArrayList<MoodEvent> getMoodList() {
        return moodList;
    }

    public void clearMoods() {
        moodList.clear();
    }

    //Sorts the mood list in reverse chronological order
    public void sortByDate() {
        Collections.sort(moodList, (mood1, mood2) -> {
            int dateComparison = mood2.getDate().compareTo(mood1.getDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            // If dates are equal, compare times
            return mood2.getTime().compareTo(mood1.getTime());
        });
    }

    // Filters the mood list to show only moods of a specific type
    public ArrayList<MoodEvent> filterByMood(Mood moodType) {
        ArrayList<MoodEvent> filteredList = new ArrayList<>();
        for (MoodEvent mood : moodList) {
            if (mood.getMood() == moodType) {
                filteredList.add(mood);
            }
        }
        return filteredList;
    }
} 