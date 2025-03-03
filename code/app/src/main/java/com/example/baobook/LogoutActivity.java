package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Class that allows user to confirm their log out
 * If the yes button is clicked the screen goes to the first screen, Login and Sign up
 * If the no button is clicked the screen goes back to the profile page
 */
public class LogoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout_activity);
        Button yes = findViewById(R.id.confirm_logout);
        Button no = findViewById(R.id.reject_logout);
        yes.setOnClickListener(v->{
            Intent intent = new Intent(LogoutActivity.this, LoginSignupSelectActivity.class);
            startActivity(intent);
        });
        no.setOnClickListener(v->{
            Intent intent = new Intent(LogoutActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

}
