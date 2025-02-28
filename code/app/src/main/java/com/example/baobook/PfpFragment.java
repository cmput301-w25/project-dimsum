package com.example.baobook;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.baobook.MainActivity;


public class PfpFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.pfp_fragment, container, false);
        Button addImage = view.findViewById(R.id.selectImageButton);
        Button nextButton = view.findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> {
            // check input
            // move to home activity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        addImage.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Add Image", Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}
