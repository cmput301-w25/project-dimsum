package com.example.baobook.controller;

import static com.example.baobook.model.PendingAction.ActionType.ADD;
import static com.example.baobook.model.PendingAction.ActionType.DELETE;
import static com.example.baobook.model.PendingAction.ActionType.EDIT;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.PendingAction;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PendingActionManager {
    private static final List<PendingAction> pendingActions = new ArrayList<>();

    public static void addAction(PendingAction action) {
        pendingActions.add(action);
    }

    public static List<PendingAction> getActions() {
        return new ArrayList<>(pendingActions);
    }

    public static void clearActions() {
        pendingActions.clear();
    }

    /**
     * Syncs pending actions with Firestore and clears them once synced.
     *
     * @param context Context for Toasts/logging
     * @param onComplete Callback to run after all syncing is finished
     */
    public static void syncPendingActions(Context context, Runnable onComplete) {
        if (pendingActions.isEmpty()) {
            Log.d("PendingActionManager", "No pending actions to sync.");
            onComplete.run();
            return;
        }

        MoodEventHelper helper = new MoodEventHelper();
        Iterator<PendingAction> iterator = pendingActions.iterator();

        syncNext(iterator, helper, context, onComplete);
    }

    private static void syncNext(Iterator<PendingAction> iterator, MoodEventHelper helper, Context context, Runnable onComplete) {
        if (!iterator.hasNext()) {
            clearActions();
            Toast.makeText(context, "Offline actions synced!", Toast.LENGTH_SHORT).show();
            onComplete.run();
            return;
        }

        PendingAction action = iterator.next();
        MoodEvent mood = action.moodEvent;

        switch (action.actionType) {
            case ADD:
                helper.publishMood(mood,
                        unused -> syncNext(iterator, helper, context, onComplete),
                        e -> {
                            Log.e("Sync", "Failed to sync ADD mood: " + e.getMessage());
                            syncNext(iterator, helper, context, onComplete);
                        });
                break;

            case EDIT:
                helper.updateMood(mood,
                        unused -> syncNext(iterator, helper, context, onComplete),
                        e -> {
                            Log.e("Sync", "Failed to sync EDIT mood: " + e.getMessage());
                            syncNext(iterator, helper, context, onComplete);
                        });
                break;

            case DELETE:
                helper.deleteMood(mood,
                        unused -> syncNext(iterator, helper, context, onComplete),
                        e -> {
                            Log.e("Sync", "Failed to sync DELETE mood: " + e.getMessage());
                            syncNext(iterator, helper, context, onComplete);
                        });
                break;

            default:
                Log.e("Sync", "Unknown action type: " + action.actionType);
                syncNext(iterator, helper, context, onComplete);
        }
    }
}
