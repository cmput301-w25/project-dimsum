package com.example;


import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference moodsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup_select_activity);
        db = FirebaseFirestore.getInstance();
        moodsRef = db.collection("moods");
    }

}
