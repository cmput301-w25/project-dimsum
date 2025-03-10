package com.example.baobook.model;

import java.io.Serializable;
import java.util.Date;

public class MoodEvent implements Serializable {
    private String username;
    private String id; // Unique ID for Firestore
    private Mood mood;
    private Date date; // Use java.util.Date for date
    private Date time; // Use java.util.Date for time
    private String description;
    private String social;

    // No-argument constructor required for Firestore
    public MoodEvent() {}

    public MoodEvent(String username, String id, Mood mood, Date date, Date time, String description, String social) {
        this.username = username;
        this.id = id;
        this.mood = mood;
        this.date = date;
        this.time = time;
        this.description = description;
        this.social = social;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    /**
     * Edits the current mood event with new details.
     *
     * @param newMood       The new mood (e.g., "Happy", "Sad", etc.).
     * @param newDate       The new date for the event.
     * @param newTime       The new time for the event.
     * @param newDescription The new description for the event.
     */
    public void editMoodEvent(Mood newMood, Date newDate, Date newTime, String newDescription, String newSocial) {
        setMood(newMood);
        setDate(newDate);
        setTime(newTime);
        setDescription(newDescription);
        setSocial(newSocial);
    }
}