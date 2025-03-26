package com.example.baobook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.PendingActionManager;

public class ConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            for (PendingAction action : PendingActionManager.getActions()) {
                switch (action.actionType) {
                    case ADD:
                        new MoodEventHelper().publishMood(action.moodEvent, aVoid -> {}, e -> {});
                        break;
                    case EDIT:
                        new MoodEventHelper().updateMood(action.moodEvent, aVoid -> {}, e -> {});
                        break;
                    case DELETE:
                        new MoodEventHelper().deleteMood(action.moodEvent, aVoid -> {}, e -> {});
                        break;
                }
            }
            PendingActionManager.clearActions();
        }
    }
}
