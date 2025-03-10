package com.example.baobook.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Date;
import java.sql.Time;
import java.util.UUID;

public class MoodEvent implements Serializable {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String username;
    private String id; // Unique ID for Firestore
    private Mood mood;
    private Date date; // Use java.util.Date for date
    private Date time; // Use java.util.Date for time
    private String description;
    private String social;
    private String base64image;

    // No-argument constructor required for Firestore
    public MoodEvent() {}

    public MoodEvent(String username, String id, Mood mood, Date date, Date time, String description, String social, String base64image) {
        this.username = username;
        this.id = id;
        this.mood = mood;
        this.date = date;
        this.time = time;
        this.description = description;
        this.social = social;
        this.base64image = base64image;
    }

    // Constructor for testing - converts java.sql.Time to java.util.Date
    public MoodEvent(String username, Mood mood, Date date, Time time, String description) {
        this.username = username;
        this.id = UUID.randomUUID().toString();
        this.mood = mood;
        this.date = date;
        this.time = new Date(time.getTime()); // Convert sql.Time to util.Date
        this.description = description;
        this.social = "ALONE";
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

    public String getBase64image() {return base64image;}

    public void setBase64image(String base64image) {this.base64image = base64image;}

    /**
     * Edits the current mood event with new details.
     *
     * @param newMood       The new mood (e.g., "Happy", "Sad", etc.).
     * @param newDate       The new date for the event.
     * @param newTime       The new time for the event.
     * @param newDescription The new description for the event.
     */
    public void editMoodEvent(Mood newMood, Date newDate, Time newTime, String newDescription) {
        setMood(newMood);
        setDate(newDate);
        setTime(new Date(newTime.getTime()));
        setDescription(newDescription);
    }

    public void editMoodEvent(Mood newMood, Date newDate, Date newTime, String newDescription, String newSocial) {
        setMood(newMood);
        setDate(newDate);
        setTime(new Date(newTime.getTime()));
        setDescription(newDescription);
        setSocial(newSocial);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == MoodEvent.class) {
            MoodEvent moodEvent = (MoodEvent) obj;
            return this.getId().equals(moodEvent.getId());  // same MoodEvent id
        }
        return false;
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map MoodEvent to string.");
        }
    }


}