package com.example;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginSignupSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup_select_activity);
        Button loginButton = findViewById(R.id.LoginButton);
        Button signupButton = findViewById(R.id.SignUpButton);
        loginButton.setOnClickListener(v -> {
            // Navigate to the login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
        signupButton.setOnClickListener(v -> {
            // Handle signup button click
            // Navigate to the signup activity
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
