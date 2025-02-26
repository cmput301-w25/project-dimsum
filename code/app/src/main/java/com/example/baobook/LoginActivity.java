package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    protected void OnCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Button loginButton = findViewById(R.id.loginButton);
        Button SignupButton = findViewById(R.id.SignUpButton);
        loginButton.setOnClickListener(v -> {
            // check input
            // Navigate to home screen
            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
        SignupButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
