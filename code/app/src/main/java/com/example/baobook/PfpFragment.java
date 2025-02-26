package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class PfpFragment extends Fragment {
    public View OnCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.signup_pfp_fragment, container, false);
        Button nextButton = view.findViewById(R.id.button);
        nextButton.setOnClickListener(v -> {
            // check input
            // move to home activity
            Toast.makeText(getActivity(),"Signup Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        return view;
    }
}
