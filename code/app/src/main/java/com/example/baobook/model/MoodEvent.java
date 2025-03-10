/**
 * Represents a mood event with associated metadata, such as mood type, date, description, and social setting.
 */

package com.example.baobook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Serializable class representing a user's mood event, designed for Firestore integration.
 */
public class MoodEvent implements Serializable {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String username;
    private String id; // Unique ID for Firestore
    private Mood mood;
    private long timestamp; // For deserializing from Firestore

    @Exclude
    @JsonIgnore
    private OffsetDateTime dateTime;
    private String description; // trigger description
    private String base64image;
    private SocialSetting social;

    // No-argument constructor required for Firestore
    public MoodEvent() {}

    /**
     * Constructs a MoodEvent with provided details.
     *
     * @param username    username associated with the event
     * @param id          unique identifier for the event
     * @param mood        mood type
     * @param dateTime    date and time of the mood event
     * @param description description or trigger
     * @param social      social setting of the mood event
     * @param base64image base64 encoded image string
     */
    public MoodEvent(String username, String id, Mood mood, OffsetDateTime dateTime, String description, SocialSetting social, String base64image) {
        this.username = username;
        this.id = id;
        this.mood = mood;
        this.timestamp = dateTime.toInstant().toEpochMilli();
        this.dateTime = dateTime;
        this.description = description;
        this.social = social;
        this.base64image = base64image;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }

    public Mood getMood() { return mood; }
    public void setMood(Mood mood) { this.mood = mood; }

    @Exclude
    public OffsetDateTime getDateTime() { return dateTime; }

    @Exclude
    public void setDateTime(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
        this.timestamp = dateTime.toInstant().toEpochMilli();
    }

    @PropertyName("timestamp")
    public long getDateTimeInMilli() {
        return timestamp;
    }
    @PropertyName("timestamp")
    public void setDateTimeInMilli(long timestamp) {
        this.timestamp = timestamp;
        this.dateTime = OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneOffset.UTC);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SocialSetting getSocial() { return social; }
    public void setSocial(SocialSetting social) { this.social = social; }

    public String getBase64image() {return base64image;}

    public void setBase64image(String base64image) {this.base64image = base64image;}

    /**
     * Edits the current mood event with new details.
     *
     * @param newMood       The new mood (e.g., "Happy", "Sad", etc.).
     * @param newDateTime  The new date and time for the event.
     * @param newDescription The new description for the event.
     * @param newSocial     The new social setting for the event.
     */
    public void editMoodEvent(Mood newMood, OffsetDateTime newDateTime, String newDescription, SocialSetting newSocial) {
        setMood(newMood);
        setDateTime(newDateTime);
        setDescription(newDescription);
        setSocial(newSocial);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MoodEvent other = (MoodEvent) obj;
        return id.equals(other.getId())
                && mood == other.getMood()
                && timestamp == other.getDateTimeInMilli()
                && description.equals(other.getDescription())
                && social == other.getSocial();
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map MoodEvent to JSON string.");
        }
    }
}
