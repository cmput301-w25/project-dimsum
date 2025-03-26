package com.example.baobook.model;

import com.example.baobook.PendingAction;

import java.util.ArrayList;
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

}
