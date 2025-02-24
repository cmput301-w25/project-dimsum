package com.example;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class SignupUsernameFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.signup_username_fragment, container, false);
        Button nextButton = rootView.findViewById(R.id.button);
        nextButton.setOnClickListener(v -> {
            // check input
            ((SignupActivity) getActivity()).replaceFragment(new PfpFragment()); // Move to next fragment
        });
        return rootView;
    }

}
