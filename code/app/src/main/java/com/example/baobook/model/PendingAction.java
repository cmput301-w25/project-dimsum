package com.example.baobook.model;

import com.example.baobook.model.MoodEvent;

public class PendingAction {
    public enum ActionType { ADD, EDIT, DELETE }
    public ActionType actionType;
    public MoodEvent moodEvent;

    public PendingAction(ActionType actionType, MoodEvent moodEvent) {
        this.actionType = actionType;
        this.moodEvent = moodEvent;
    }
}
