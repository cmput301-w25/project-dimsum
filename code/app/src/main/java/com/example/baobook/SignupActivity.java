package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.baobook.SignupUsernameFragment;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);
        // for now just go to signup username fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignupUsernameFragment()) // Replace with your container ID
                .commit();
//        if (savedInstanceState == null) {
//            replaceFragment(new SignupUsernameFragment());
//        }
    }
    public void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
