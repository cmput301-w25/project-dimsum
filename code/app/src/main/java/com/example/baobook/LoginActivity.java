package com.example.baobook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.controller.AuthHelper;
import com.google.android.material.snackbar.Snackbar;

/*
//Login activity where user can enter their existing credentials and log in
to their account. Uses the AuthHelper class to authenticate the user
 */
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Button loginButton = findViewById(R.id.loginButton);
        Button SignupButton = findViewById(R.id.SignUpButton);
        EditText username = findViewById(R.id.usernameInput);
        EditText password = findViewById(R.id.passwordInput);

        //hide status bar at the top
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        loginButton.setOnClickListener(v -> {
            // check input
            String usernameText = username.getText().toString();
            String passwordText = password.getText().toString();
            if(usernameText.isEmpty() || passwordText.isEmpty()){
                Snackbar.make(v, "Please enter all fields", Snackbar.LENGTH_SHORT).show();
                return;
            }
            // check if username and password match existing user
            AuthHelper authHelper = new AuthHelper(this);
            authHelper.loginUser(usernameText, passwordText,
                    aVoid -> {
                        // Successful login
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();

                        // Navigate to Home screen
                        Intent intent = new Intent(LoginActivity.this, Home.class);
                        startActivity(intent);
                        finish();
                    },
                    e -> {
                        // Login failed (incorrect username or password)
                        Toast.makeText(LoginActivity.this, "Invalid login.", Toast.LENGTH_SHORT).show();
                    }
            );
        });
        SignupButton.setOnClickListener(v-> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
