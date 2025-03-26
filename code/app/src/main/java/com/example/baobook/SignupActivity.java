package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.controller.AuthHelper;
import com.google.android.material.snackbar.Snackbar;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);
        Button nextButton = findViewById(R.id.next_button);
        EditText usernameInput = findViewById(R.id.signup_username_input);
        EditText passwordInput = findViewById(R.id.signup_password_input);
        nextButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Snackbar.make(view, "Please enter all fields", Snackbar.LENGTH_SHORT).show();
                return;  //  Stop execution here if input is empty
            }

            // Check if username exists
            AuthHelper authHelper = new AuthHelper(this);
            authHelper.userExists(username, exists -> {
                if (exists) {
                    Snackbar.make(view, "Username already exists", Snackbar.LENGTH_SHORT).show();
                    return;  //  Stop execution here if username already exists
                }
                authHelper.registerUser(username, password,
                        aVoid -> {
                            Toast.makeText(SignupActivity.this, "Signup Successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignupActivity.this, Home.class);
                            startActivity(intent);
                            finish();
                        }, e -> {
                            Toast.makeText(SignupActivity.this, "Signup Failed. Try Again.", Toast.LENGTH_LONG).show();
                        });
            }, e -> {
                Toast.makeText(SignupActivity.this, "Something went wrong. Please try again later", Toast.LENGTH_LONG).show();
            });
        });

    }

}
