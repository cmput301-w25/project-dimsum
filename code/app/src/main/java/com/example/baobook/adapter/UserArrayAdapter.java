package com.example.baobook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.baobook.R;
import com.example.baobook.model.User;

import java.util.ArrayList;

// ArrayAdapter for the lists of users in the following and followers
public class UserArrayAdapter extends ArrayAdapter<User> {
    private final String activity;
    private final OnAcceptListener onAcceptListener;
    public UserArrayAdapter(Context context, ArrayList<User> users, String activity, OnAcceptListener onAcceptListener) {
        super(context, 0, users);
        this.activity = activity;
        this.onAcceptListener = onAcceptListener;
    }

    public interface OnAcceptListener {
        void onAccept(User user, int position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.follow_content, parent, false);
            holder = new ViewHolder();
            holder.username = convertView.findViewById(R.id.username);
            holder.acceptButton = convertView.findViewById(R.id.accept_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = getItem(position);
        if (user != null) {
            holder.username.setText(user.getUsername());
        }

        if ("FollowRequestsActivity".equals(activity)) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setOnClickListener(v -> {
                if (onAcceptListener != null) {
                    onAcceptListener.onAccept(user, position); // Notify activity
                }
            });
        } else {
            holder.acceptButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView username;
        Button acceptButton;
    }

    public void removeUser(int position) {
        remove(getItem(position)); // Remove user from the list
        notifyDataSetChanged();
    }
}
