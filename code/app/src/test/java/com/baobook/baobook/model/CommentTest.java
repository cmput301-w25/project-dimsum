package com.baobook.baobook.model;

import com.example.baobook.model.User;

import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;

import static org.junit.Assert.*;

import com.example.baobook.model.Comment;
import com.example.baobook.model.User;
import org.junit.Before;
import org.junit.Test;

public class CommentTest {
    private String text= "This is a comment";
    private User author = new User("user123", "123", null, null);
    private String moodEventId = "0";
    private Comment comment;
    @Before
    public void setUp() {
        comment = new Comment(moodEventId, author, text);
    }

    @Test
    public void testConstructor() {
        assertEquals("0", comment.getMoodEvent());
        assertEquals(author, comment.getAuthor());
        assertEquals("This is a comment", comment.getText());
    }

    @Test
    public void testSetMoodEventId() {
        comment.setMoodEventId("newMood456");
        assertEquals("newMood456", comment.getMoodEvent());
    }

    @Test
    public void testSetAuthor() {
        User newAuthor = new User("user456", "123", null, null);
        comment.setAuthor(newAuthor);
        assertEquals(newAuthor, comment.getAuthor());
    }

    @Test
    public void testSetText() {
        comment.setText("Updated comment text.");
        assertEquals("Updated comment text.", comment.getText());
    }

    @Test
    public void testGetAuthorUsername() {
        assertEquals("user123", comment.getAuthorUsername());
    }

    @Test
    public void testDefaultConstructor() {
        Comment defaultComment = new Comment();
        assertNull(defaultComment.getMoodEvent());
        assertNull(defaultComment.getAuthor());
        assertNull(defaultComment.getText());
    }
}

