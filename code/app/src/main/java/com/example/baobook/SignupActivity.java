package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class SignupActivity extends AppCompatActivity {
    protected void OnCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        if (savedInstanceState == null) {
            replaceFragment(new SignupUsernameFragment());
        }
    }
    public void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
