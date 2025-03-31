package com.example.baobook;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
