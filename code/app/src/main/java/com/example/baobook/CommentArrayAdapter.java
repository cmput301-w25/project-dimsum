package com.example.baobook;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.baobook.model.Comment;
import com.example.baobook.model.User;


import java.util.ArrayList;

/**
 * Adapter for comments. Each comment consists of a text and an author. Comment
 * Array Adapter is used inside of Comment Activity
 */
public class CommentArrayAdapter extends ArrayAdapter<Comment> {

    public CommentArrayAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        Log.d("CommentArrayAdapter", "getView: position=" + position);

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, parent, false);
        } else {
            view = convertView;
        }
        Comment comment = getItem(position);
        TextView commentText = view.findViewById(R.id.comment_text);
        TextView authorText = view.findViewById(R.id.author_text);
        if (comment != null) {
            commentText.setText(comment.getText());
            authorText.setText(comment.getAuthorUsername());
        }
        return view;
    }
}
