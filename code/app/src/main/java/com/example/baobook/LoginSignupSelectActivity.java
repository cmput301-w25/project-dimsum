package com.example.baobook;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginSignupSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup_select_activity);
        Button loginButton = findViewById(R.id.LoginButton);
        Button signupButton = findViewById(R.id.SignUpButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignupSelectActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginSignupSelectActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
