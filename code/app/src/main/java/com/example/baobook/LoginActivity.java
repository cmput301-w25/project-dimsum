package com.example.baobook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.MainActivity;
import com.example.baobook.controller.AuthHelper;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Button loginButton = findViewById(R.id.loginButton);
        Button SignupButton = findViewById(R.id.SignUpButton);
        EditText username = findViewById(R.id.usernameInput);
        EditText password = findViewById(R.id.passwordInput);
        loginButton.setOnClickListener(v -> {
            // check input
            String usernameText = username.getText().toString();
            String passwordText = password.getText().toString();
            if(usernameText.isEmpty() || passwordText.isEmpty()){
                Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
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
    public void saveLogin(String userId){
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", userId);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }
}
