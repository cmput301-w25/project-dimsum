package com.example.baobook.model;

/**
 * Represents a comment on a MoodEvent.
 */
public class Comment {
    private String moodEventId;
    private User Author;
    private String text;

    public Comment(String moodEventId, User Author, String text) {
        this.moodEventId = moodEventId;
        this.Author = Author;
        this.text = text;
    }
    public Comment() {
        // Default constructor required for Firestore
    }
    public String getMoodEvent() {
        return moodEventId;
    }

    public String getText() {
        return text;
    }
    public void setMoodEventId(String moodEventId) {
        this.moodEventId = moodEventId;
    }
    public void setAuthor(User Author) {
        this.Author = Author;
    }
    public void setText(String text) {
        this.text = text;
    }
    public User getAuthor() {
        return Author;
    }
    public String getAuthorUsername(){
        return getAuthor().getUsername();
    }
}
