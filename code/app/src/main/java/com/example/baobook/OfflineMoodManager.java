package com.example.baobook;

import android.content.Context;
import android.util.Log;

import com.example.baobook.NetworkUtil;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages offline mood events, storing them in SharedPreferences for simplicity.
 * Handles add, edit, and delete operations when offline.
 */
public class OfflineMoodManager {

    private static final String PREFS_NAME = "OfflineMoods";
    private static final String PENDING_ADDITIONS = "PendingAdditions";
    private static final String PENDING_DELETIONS = "PendingDeletions";
    private static final String PENDING_EDITS = "PendingEdits";
    private final Context context;

    public OfflineMoodManager(Context context) {
        this.context = context;
    }

    public void savePendingAddition(MoodEvent moodEvent) {
        List<MoodEvent> pendingAdditions = getPendingMoods(PENDING_ADDITIONS);
        pendingAdditions.add(moodEvent);
        savePendingMoods(PENDING_ADDITIONS, pendingAdditions);
    }

    public void savePendingDeletion(MoodEvent moodEvent) {
        List<MoodEvent> pendingDeletions = getPendingMoods(PENDING_DELETIONS);
        pendingDeletions.add(moodEvent);
        savePendingMoods(PENDING_DELETIONS, pendingDeletions);
    }

    public void savePendingEdit(MoodEvent moodEvent) {
        List<MoodEvent> pendingEdits = getPendingMoods(PENDING_EDITS);
        pendingEdits.removeIf(m -> m.getId().equals(moodEvent.getId())); // Remove old edit if exists
        pendingEdits.add(moodEvent);
        savePendingMoods(PENDING_EDITS, pendingEdits);
    }

    public void syncPendingMoods() {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            Log.d("OfflineMoodManager", "No network available for syncing.");
            return;
        }

        List<MoodEvent> pendingAdditions = getPendingMoods(PENDING_ADDITIONS);
        List<MoodEvent> pendingDeletions = getPendingMoods(PENDING_DELETIONS);
        List<MoodEvent> pendingEdits = getPendingMoods(PENDING_EDITS);

        for (MoodEvent moodEvent : pendingAdditions) {
            Log.d("OfflineMoodManager", "Syncing addition: " + moodEvent);
        }

        for (MoodEvent moodEvent : pendingDeletions) {
            Log.d("OfflineMoodManager", "Syncing deletion: " + moodEvent);
        }

        for (MoodEvent moodEvent : pendingEdits) {
            Log.d("OfflineMoodManager", "Syncing edit: " + moodEvent);
        }

        clearPendingMoods(PENDING_ADDITIONS);
        clearPendingMoods(PENDING_DELETIONS);
        clearPendingMoods(PENDING_EDITS);
    }

    public ArrayList<MoodEvent> getAllMoods() {
        return new ArrayList<>(getPendingMoods(PENDING_ADDITIONS));
    }

    private List<MoodEvent> getPendingMoods(String key) {
        Set<String> moodStrings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getStringSet(key, new HashSet<>());

        List<MoodEvent> moods = new ArrayList<>();
        for (String moodString : moodStrings) {
            MoodEvent mood = deserializeMood(moodString);
            if (mood != null) {
                moods.add(mood);
            }
        }
        return moods;
    }

    private void savePendingMoods(String key, List<MoodEvent> moods) {
        Set<String> moodStrings = new HashSet<>();
        for (MoodEvent mood : moods) {
            moodStrings.add(serializeMood(mood));
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(key, moodStrings)
                .apply();
    }

    private void clearPendingMoods(String key) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply();
    }

    private String serializeMood(MoodEvent mood) {
        return mood.getId() + "," + mood.getMood() + "," + mood.getDateTimeInMilli();
    }

    private MoodEvent deserializeMood(String moodString) {
        String[] parts = moodString.split(",");
        if (parts.length < 3) return null;

        MoodEvent mood = new MoodEvent();
        mood.setId(parts[0]);
        mood.setMood(Mood.valueOf(parts[1])); // Ensure Mood enum parsing is correct
        mood.setDateTimeInMilli(Long.parseLong(parts[2]));
        return mood;
    }

}
