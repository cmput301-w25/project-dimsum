package com.example.baobook;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.baobook.controller.AuthHelper;
import com.google.android.material.snackbar.Snackbar;
//Fragment for asking for username and password for sign up. Will register user and store their credentials in
//firebase. Include input validation and uses AuthHelper to register user.
public class SignupUsernameFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup_username_fragment, container, false);
        Button nextButton = rootView.findViewById(R.id.button);
        EditText usernameInput = rootView.findViewById(R.id.signup_username_input);
        EditText passwordInput = rootView.findViewById(R.id.signup_password_input);

        nextButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Snackbar.make(view, "Please enter all fields", Snackbar.LENGTH_SHORT).show();
                return;  //  Stop execution here if input is empty
            }

            // Check if username exists
            AuthHelper authHelper = new AuthHelper(getContext());
            authHelper.userExists(username, exists -> {
                if (exists) {
                    Snackbar.make(view, "Username already exists", Snackbar.LENGTH_SHORT).show();
                    return;  //  Stop execution here if username already exists
                }
                authHelper.registerUser(username, password,
                        aVoid -> {
                            Toast.makeText(getActivity(), "Signup Successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getActivity(), Home.class);
                            startActivity(intent);
                            requireActivity().finish();
                        }, e -> {
                            Toast.makeText(getActivity(), "Signup Failed. Try Again.", Toast.LENGTH_LONG).show();
                        });
                }, e -> {
                    Toast.makeText(getActivity(), "Something went wrong. Please try again later", Toast.LENGTH_LONG).show();
            });
        });

        return rootView;
    }
}

