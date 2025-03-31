package com.example.baobook.model;

import com.example.baobook.model.MoodEvent;

public class PendingAction {
    public enum ActionType { ADD, EDIT, DELETE }
    public ActionType actionType;
    public MoodEvent moodEvent;


    /**
     * Constructs a new {@code PendingAction} with the given type and mood event.
     *
     * @param actionType the type of action to be performed
     * @param moodEvent  the {@link MoodEvent} the action applies to
     */
    public PendingAction(ActionType actionType, MoodEvent moodEvent) {
        this.actionType = actionType;
        this.moodEvent = moodEvent;
    }
}
