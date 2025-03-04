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

import com.example.baobook.model.User;

public class SignupUsernameFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup_username_fragment, container, false);
        Button nextButton = rootView.findViewById(R.id.button);
        EditText usernameInput = rootView.findViewById(R.id.signup_username_input);
        EditText passwordInput = rootView.findViewById(R.id.signup_password_input);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(getActivity(), "Please enter valid username and password", Toast.LENGTH_SHORT).show();
                }
                //check username and password requirements
                //if pass requirements, create user
                User user = new User(username, password);
                //save user to database
                Toast.makeText(getActivity(), "Signup Successful", Toast.LENGTH_LONG).show();
                // we can add pfp page after, should go straight to home for now
//                if (getActivity() instanceof SignupActivity) {
//                    ((SignupActivity) getActivity()).replaceFragment(new PfpFragment()); // Move to next fragment
//                }
                Intent intent = new Intent(getActivity(), Home.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return rootView;
    }
}

