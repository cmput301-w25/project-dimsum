package com.example.baobook.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


/**
 * Represents a mood event created by a user.
 * A MoodEvent contains details like the mood, time, location, privacy setting, description, and an optional image.
 * Implements {@link Parcelable} for easy transmission between Android components.
 */
public class MoodEvent implements Parcelable {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String username;
    private String id;
    private Mood mood;
    private long timestamp; // Firestore-compatible
    private Privacy privacy;

    @Exclude
    @JsonIgnore
    private OffsetDateTime dateTime;

    private String description;
    private SocialSetting social;
    private String base64image;
    private GeoPoint location;

    public MoodEvent() {}

    /**
     * Constructs a MoodEvent with the specified parameters.
     *
     * @param username    the user who created the event
     * @param id          unique ID for the event
     * @param mood        mood being recorded
     * @param dateTime    date and time of the mood
     * @param description optional text description
     * @param social      social setting of the mood
     * @param base64image base64-encoded image string
     * @param privacy     privacy level of the event
     */

    public MoodEvent(String username, String id, Mood mood, OffsetDateTime dateTime,
                     String description, SocialSetting social, String base64image, Privacy privacy) {
        this.username = username;
        this.id = id;
        this.mood = mood;
        this.dateTime = dateTime;
        this.timestamp = dateTime.toInstant().toEpochMilli();
        this.description = description;
        this.social = social;
        this.base64image = base64image;
        this.privacy = privacy;
    }

    /**
     * Constructs a MoodEvent with an additional location.
     */

    public MoodEvent(String username, String id, Mood mood, OffsetDateTime dateTime,
                     String description, SocialSetting social, String base64image, Privacy privacy,
                     GeoPoint location) {
        this(username, id, mood, dateTime, description, social, base64image, privacy);
        this.location = location;
    }

    public String getUsername() { return username; }
    public String getId() { return id; }
    public Mood getMood() { return mood; }

    /**
     * Returns the mood event's timestamp as {@link OffsetDateTime}.
     *
     * @return event date and time
     */

    @Exclude
    public OffsetDateTime getDateTime() { return dateTime; }


    /**
     * Sets the mood event's timestamp and updates the epoch time for Firestore.
     */
    @Exclude
    public void setDateTime(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
        this.timestamp = dateTime.toInstant().toEpochMilli();
    }

    @PropertyName("timestamp")
    public long getDateTimeInMilli() { return timestamp; }

    @PropertyName("timestamp")
    public void setDateTimeInMilli(long timestamp) {
        this.timestamp = timestamp;
        this.dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }

    public String getDescription() { return description; }
    public SocialSetting getSocial() { return social; }
    public String getBase64image() { return base64image; }
    public Privacy getPrivacy() { return privacy; }
    public GeoPoint getLocation() { return location; }

    public void setLocation(GeoPoint location) { this.location = location; }
    public void setMood(Mood mood) { this.mood = mood; }
    public void setDescription(String description) { this.description = description; }
    public void setSocial(SocialSetting social) { this.social = social; }
    public void setBase64image(String base64image) { this.base64image = base64image; }
    public void setPrivacy(Privacy privacy) { this.privacy = privacy; }
    public void setId(String id) { this.id = id; }



    /**
     * Updates this MoodEvent's editable fields.
     *
     * @param mood        new mood
     * @param dateTime    new date and time
     * @param description new description
     * @param social      new social setting
     * @param privacy     new privacy setting
     */
    public void editMoodEvent(Mood mood, OffsetDateTime dateTime, String description,
                              SocialSetting social, Privacy privacy) {
        setMood(mood);
        setDateTime(dateTime);
        setDescription(description);
        setSocial(social);
        setPrivacy(privacy);
    }


    /**
     * Updates this MoodEvent with another MoodEvent's data.
     *
     * @param updatedMoodEvent the updated event to copy from
     */
    public void updateMoodEvent(MoodEvent updatedMoodEvent) {
        setMood(updatedMoodEvent.getMood());
        setDateTime(updatedMoodEvent.getDateTime());
        setDescription(updatedMoodEvent.getDescription());
        setSocial(updatedMoodEvent.getSocial());
        setPrivacy(updatedMoodEvent.getPrivacy());
        setBase64image(updatedMoodEvent.getBase64image());
        setLocation(updatedMoodEvent.getLocation());
    }

    protected MoodEvent(Parcel in) {
        username = in.readString();
        id = in.readString();
        mood = Mood.valueOf(in.readString());
        long seconds = in.readLong();
        int nanos = in.readInt();
        dateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
        timestamp = in.readLong();
        description = in.readString();
        social = SocialSetting.valueOf(in.readString());
        base64image = in.readString();
        privacy = Privacy.valueOf(in.readString());
        boolean hasLocation = in.readByte() != 0;
        if (hasLocation) {
            double lat = in.readDouble();
            double lng = in.readDouble();
            location = new GeoPoint(lat, lng);
        }
    }

    public static final Creator<MoodEvent> CREATOR = new Creator<MoodEvent>() {
        @Override
        public MoodEvent createFromParcel(Parcel in) {
            return new MoodEvent(in);
        }

        @Override
        public MoodEvent[] newArray(int size) {
            return new MoodEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(id);
        dest.writeString(mood.name());
        dest.writeLong(dateTime.toInstant().getEpochSecond());
        dest.writeInt(dateTime.getNano());
        dest.writeLong(timestamp);
        dest.writeString(description);
        dest.writeString(social.name());
        dest.writeString(base64image);
        dest.writeString(privacy.name());
        if (location != null) {
            dest.writeByte((byte) 1);
            dest.writeDouble(location.getLatitude());
            dest.writeDouble(location.getLongitude());
        } else {
            dest.writeByte((byte) 0);
        }
    }

    /**
     * Converts the MoodEvent to its JSON string representation.
     *
     * @return JSON string of the MoodEvent or a fallback if serialization fails
     */

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            return "MoodEvent{error_serializing_to_json}";
        }
    }
}
