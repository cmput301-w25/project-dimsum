package com.example.baobook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.adapter.CommentArrayAdapter;
import com.example.baobook.model.Comment;
import com.example.baobook.util.UserSession;
import com.example.baobook.controller.MoodEventHelper;
import java.util.ArrayList;

/**
 * Represents a comment on a MoodEvent. Starts when a user clicks on a comment button from a mood event.
 * User can see all posted comments and post new ones.
 */
public class CommentActivity extends AppCompatActivity {
    private EditText commentInput;
    private CommentArrayAdapter commentAdapter;
    private ArrayList<Comment> comments;
    private UserSession userSession;
    private MoodEventHelper MoodEventHelper;

    private String moodEventId;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_activity);
        MoodEventHelper = new MoodEventHelper();
        comments = new ArrayList<>();
        commentAdapter = new CommentArrayAdapter(this, comments);

        Button backButton = findViewById(R.id.back_button);
        Button postCommentButton = findViewById(R.id.post_comment_button);
        commentInput = findViewById(R.id.comment_input);
        ListView commentListView = findViewById(R.id.comment_list_view);

        commentListView.setAdapter(commentAdapter);
        moodEventId = getIntent().getStringExtra("MOOD_EVENT_ID");
        userSession = new UserSession(this);

        MoodEventHelper.loadComments(moodEventId, commentsSnapshot -> {
            comments.clear();
            for (Comment comment : commentsSnapshot) {
                comments.add(comment);
                commentAdapter.notifyDataSetChanged();
            }
        }, e -> {
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
        });

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString();
            if (!commentText.isEmpty()) {
                Comment newComment = new Comment(moodEventId, userSession.getUser(), commentText);
                comments.add(newComment);
                commentAdapter.notifyDataSetChanged();
                MoodEventHelper.addComment(moodEventId, newComment, null, null);
                Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show();
            }
        });
        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}
