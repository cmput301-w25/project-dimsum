package com.example.baobook;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.baobook.FirestoreHelper;
import com.example.baobook.Home;
import com.example.baobook.model.User;

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
                Toast.makeText(requireActivity(), "Please enter a valid username and password", Toast.LENGTH_SHORT).show();
                return;  //  Stop execution here if input is empty
            }

            // Check if username exists
            FirestoreHelper.checkIfUsernameExists(username, new FirestoreHelper.UsernameExistsCallback() {
                @Override
                public void onResult(boolean exists) {
                    if (exists) {
                        Toast.makeText(requireActivity(), "Username already exists", Toast.LENGTH_SHORT).show();
                        return;  //  Stop execution here if username exists
                    }

                    // If username is unique, create the user
                    User user = new User(username, password);

                    // Save username in SharedPreferences
                    SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Username", username);
                    editor.apply();

                    // Add user to Firestore
                    FirestoreHelper.addUser(user, new FirestoreHelper.UserCallback() {
                        @Override
                        public void onResult(boolean success) {
                            if (success) {
                                Toast.makeText(getActivity(), "Signup Successful", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), Home.class);
                                startActivity(intent);
                                requireActivity().finish();
                            } else {
                                Toast.makeText(getActivity(), "Signup Failed. Try Again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        });

        return rootView;
    }
}

