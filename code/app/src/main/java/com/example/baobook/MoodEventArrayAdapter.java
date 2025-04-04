package com.example.baobook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.Privacy;
import com.example.baobook.util.MoodUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

// Adapter used to display MoodEvent items in a ListView. Each mood item shows details like
//username, mood type (with emoji and color), timestamp, description, social situation, and an optional image.

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private MoodEventOptionsFragment.MoodEventOptionsDialogListener listener;

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> moods) {
        super(context, 0, moods);
        if (context instanceof MoodEventOptionsFragment.MoodEventOptionsDialogListener) {
            listener = (MoodEventOptionsFragment.MoodEventOptionsDialogListener) context;
        }
    }

    private Bitmap base64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        Log.d("MoodEventArrayAdapter", "getView: position=" + position);

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content, parent, false);
        } else {
            view = convertView;
        }

        MoodEvent moodEvent = getItem(position);

        TextView usernameText = view.findViewById(R.id.username_text);
        TextView moodText = view.findViewById(R.id.mood_state);
        TextView dateText = view.findViewById(R.id.mood_date);
        TextView timeText = view.findViewById(R.id.mood_time);
        TextView descriptionText = view.findViewById(R.id.mood_description);
        TextView publicText = view.findViewById(R.id.public_text);
        TextView social = view.findViewById(R.id.social_situation);
        View rootLayout = view.findViewById(R.id.mood_item_root);
        ImageView moodImage = view.findViewById(R.id.mood_image);
        Button commentButton = view.findViewById(R.id.comment_button);

        if (moodEvent != null) {
            // Set username
            usernameText.setText("Posted by: " + moodEvent.getUsername());

            // Format date and time
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

            // Set mood state with emoji and color using MoodUtils
            String moodString = moodEvent.getMood().toString();
            moodText.setText(MoodUtils.getMoodEmoji(moodString) + " " + moodString); // Add emoji
            moodText.setTextColor(MoodUtils.getMoodColor(moodString)); // Set color

            // Set date, time, and description
            dateText.setText("Date: " + dateFormat.format(moodEvent.getDateTime().toLocalDate()));
            timeText.setText("Time: " + timeFormat.format(moodEvent.getDateTime().toLocalTime()));
            descriptionText.setText("Trigger: " + moodEvent.getDescription());
            publicText.setText(moodEvent.getPrivacy()== Privacy.PRIVATE ? "Private" : "Public");
            String description = moodEvent.getDescription();
            if (description == null || description.trim().isEmpty()) {
                descriptionText.setVisibility(View.GONE);
            } else {
                descriptionText.setVisibility(View.VISIBLE);
                descriptionText.setText("Trigger: " + description);
            }

            social.setText("Social: " + moodEvent.getSocial());

            if (moodEvent.getBase64image() != null && !moodEvent.getBase64image().isEmpty()) {
                Log.d("MoodEventArrayAdapter", "Base64 Image Found: " + moodEvent.getBase64image().substring(0, 30)); // Show only first 30 chars
                Bitmap bitmap = base64ToBitmap(moodEvent.getBase64image());
                moodImage.setImageBitmap(bitmap);
            } else {
                Log.d("MoodEventArrayAdapter", "No Image Found, using default.");
                moodImage.setImageResource(R.drawable.cat_image);
            }

            GradientDrawable drawable = (GradientDrawable) rootLayout.getBackground().mutate();
            if (drawable != null) {
                drawable.setStroke(5, MoodUtils.getMoodColor(moodString)); // Set border color
            }

            // Set up long press listener
            rootLayout.setOnLongClickListener(v -> {
                Log.d("MoodEventArrayAdapter", "Long press detected on mood item");
                if (listener != null) {
                    Log.d("MoodEventArrayAdapter", "Listener is not null, showing options dialog");
                    MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(moodEvent);
                    fragment.show(((androidx.fragment.app.FragmentActivity) getContext()).getSupportFragmentManager(), "MoodOptions");
                } else {
                    Log.e("MoodEventArrayAdapter", "Listener is null!");
                }
                return true;
            });
        }

        commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CommentActivity.class);
            intent.putExtra("MOOD_EVENT_ID", moodEvent.getId()); // Pass mood event ID to CommentActivity
            getContext().startActivity(intent);
        });

        return view;
    }
}
