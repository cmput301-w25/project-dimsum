package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements AddFragment.AddMoodEventDialogListener {

    private Button profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);



        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            if (v != null) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });

        // Floating Action Button to add a new mood
        FloatingActionButton fab = findViewById(R.id.add_button);
        fab.setOnClickListener(v -> openAddMoodDialog());

        profile = findViewById(R.id.profile_button);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoodHistory.class);
                startActivity(intent);
            }
        });

    }

    private void openAddMoodDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddFragment addFragment = new AddFragment();
        addFragment.show(fragmentManager, "AddMoodDialog");
    }

    @Override
    public void addMoodEvent(MoodEvent mood) {
        if (mood != null) {
            Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
            // Handle mood event addition logic if needed
        }
    }
}
