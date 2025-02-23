package com.example.baobook;

import java.sql.Time;
import java.util.Date;

public class MoodEvent {
    private Date date;
    private Time time; // Use java.sql.Time consistently
    private String description;
    private String state;

    public MoodEvent(String state, Date date, Time time, String description) {
        this.state = state;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Time getTime() { // Return java.sql.Time
        return time;
    }

    public void setTime(Time time) { // Accept java.sql.Time
        this.time = time;
    }
}