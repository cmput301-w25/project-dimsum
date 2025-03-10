package com.example.baobook;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
/*
class that allows user to choose between login and signup through buttons and also
initializes firebase app and firestore database. First screen that user sees
 */
public class LoginSignupSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup_select_activity);
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Button loginButton = findViewById(R.id.LoginButton);
        Button signupButton = findViewById(R.id.SignUpButton);
        loginButton.setOnClickListener(v-> {
                Intent intent = new Intent(LoginSignupSelectActivity.this, LoginActivity.class);
                startActivity(intent);
        });
        signupButton.setOnClickListener(v ->{
                Intent intent = new Intent(LoginSignupSelectActivity.this, SignupActivity.class);
                startActivity(intent);
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = preferences.getString("userId", null);

        if (userId != null) {
            // User is already logged in, go to main app screen
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
            finish();
        }
    }

}
