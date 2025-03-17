package com.example.baobook;

import static android.content.Intent.getIntent;

import android.os.Bundle;

public class FollowActivity {
    public void onCreate(Bundle savedInstanceState) {
        String listType = getIntent().getStringExtra("ListType");
        if (listType.equals("followers")) {
            // Display followers list
        } else if (listType.equals("following")) {
            // Display following list
        }
    }
}
