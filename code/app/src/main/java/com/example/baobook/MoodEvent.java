package com.example.baobook;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

public class MoodEvent implements Serializable {
    private String state;
    private Date date;
    private Time time;
    private String description;

    public MoodEvent(String state, Date date, Time time, String description) {
        this.state = state;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
     * @param newState       The new mood state (e.g., "Happy", "Sad", etc.).
     * @param newDate        The new date for the event.
     * @param newTime        The new time for the event.
     * @param newDescription The new description for the event.
     */
    public void editMoodEvent(String newState, Date newDate, Time newTime, String newDescription) {
        setState(newState);
        setDate(newDate);
        setTime(newTime);
        setDescription(newDescription);
    }
}