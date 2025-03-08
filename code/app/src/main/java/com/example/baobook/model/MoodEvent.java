package com.example.baobook.model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

public class MoodEvent implements Serializable {
    private Mood mood;
    private Date date;
    private Time time;
    private String description;
    private String social;

    public MoodEvent(Mood mood, Date date, Time time, String description, String social) {
        this.mood = mood;
        this.date = date;
        this.time = time;
        this.description = description;
        this.social = social;
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

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Edits the current mood event with new details.
     *
     * @param newMood       The new mood (e.g., "Happy", "Sad", etc.).
     * @param newDate        The new date for the event.
     * @param newTime        The new time for the event.
     * @param newDescription The new description for the event.
     */
    public void editMoodEvent(Mood newMood, Date newDate, Time newTime, String newDescription, String newSocial) {
        setMood(newMood);
        setDate(newDate);
        setTime(newTime);
        setDescription(newDescription);
        setSocial(newSocial);
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

}