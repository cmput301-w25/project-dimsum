package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.MainActivity;

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

            // Navigate to home screen
            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginActivity.this, Home.class);
            startActivity(intent);
        });
        SignupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
