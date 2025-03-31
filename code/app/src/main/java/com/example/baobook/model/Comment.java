package com.example.baobook.model;

/**
 * Represents a comment on a MoodEvent.
 */
public class Comment {
    private String moodEventId;
    private User Author;
    private String text;


    /**
     * Constructs a new {@code Comment} with the specified mood event ID,
     * author, and text.
     *
     * @param moodEventId the ID of the associated mood event
     * @param Author the user who authored the comment
     * @param text the content of the comment
     */
    public Comment(String moodEventId, User Author, String text) {
        this.moodEventId = moodEventId;
        this.Author = Author;
        this.text = text;
    }

    /**
     * Default no-argument constructor.
     * Required for Firestore deserialization.
     */
    public Comment() {
        // Default constructor required for Firestore
    }

    /**
     * Returns the ID of the associated mood event.
     *
     * @return the mood event ID
     */
    public String getMoodEvent() {
        return moodEventId;
    }
    /**
     * Returns the text content of the comment.
     *
     * @return the comment text
     */

    public String getText() {
        return text;
    }


    /**
     * Sets the ID of the associated mood event.
     *
     * @param moodEventId the mood event ID to set
     */
    public void setMoodEventId(String moodEventId) {
        this.moodEventId = moodEventId;
    }

    /**
     * Sets the author of the comment.
     *
     * @param Author the user to set as the author
     */
    public void setAuthor(User Author) {
        this.Author = Author;
    }
    /**
     * Sets the text content of the comment.
     *
     * @param text the comment text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the user who authored the comment.
     *
     * @return the author of the comment
     */
    public User getAuthor() {
        return Author;
    }

    /**
     * Returns the username of the comment's author.
     *
     * @return the author's username
     */
    public String getAuthorUsername(){
        return getAuthor().getUsername();
    }
}
