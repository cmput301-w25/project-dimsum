package com.example.baobook;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.baobook.model.Mood;

public class MapFilterDialogFragment extends DialogFragment {

    public interface OnFilterSaveListener {
        void onFilterSave(Mood mood, boolean lastWeek, boolean within5km, String word);
    }

    private OnFilterSaveListener listener;

    // Any existing filter states (optional)
    private Mood existingMood;
    private boolean existingLastWeek;
    private boolean existingWithin5Km;
    private String existingWord;

    public MapFilterDialogFragment(OnFilterSaveListener listener) {
        this.listener = listener;
    }

    // Optionally let the parent set existing filters (so the dialog can show them)
    public void setExistingFilters(Mood mood, boolean lastWeek, boolean within5km, String word) {
        this.existingMood = mood;
        this.existingLastWeek = lastWeek;
        this.existingWithin5Km = within5km;
        this.existingWord = word;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                requireContext(),
                com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
        );

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.map_filter_dialog, null);

        // Build an array with "No Filter" as the first item
        String[] actualMoods = getResources().getStringArray(R.array.mood_options);
        String[] moodArray = new String[actualMoods.length + 1];
        moodArray[0] = "No Filter";  // first slot => no mood filter
        System.arraycopy(actualMoods, 0, moodArray, 1, actualMoods.length);

        // Find UI elements in the layout
        Spinner spinnerMood = view.findViewById(R.id.spinner_mood);
        CheckBox checkLastWeek = view.findViewById(R.id.check_recent_week);
        CheckBox checkWithin5km = view.findViewById(R.id.check_5_km);
        EditText editWord = view.findViewById(R.id.edit_filter_word);
        Button buttonCancel = view.findViewById(R.id.button_cancel);
        Button buttonSave = view.findViewById(R.id.button_save);

        // Set up the spinner adapter
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                moodArray
        );
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);

        // If we have existing filters, apply them
        if (existingMood != null) {
            // Find index in the moodArray
            String moodName = existingMood.toString();
            int idx = -1;
            for (int i = 1; i < moodArray.length; i++) {
                if (moodArray[i].equalsIgnoreCase(moodName)) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) spinnerMood.setSelection(idx);
        }
        checkLastWeek.setChecked(existingLastWeek);
        checkWithin5km.setChecked(existingWithin5Km);
        if (existingWord != null) {
            editWord.setText(existingWord);
        }

        // Attach layout to the dialog
        builder.setView(view);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // CANCEL button: just dismiss the dialog
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        // SAVE button: gather user’s selections, pass to listener, then dismiss
        buttonSave.setOnClickListener(v -> {
            int position = spinnerMood.getSelectedItemPosition();
            Mood selectedMood = null;
            if (position > 0) {
                // The first item is "No Filter," so real moods start at index 1
                String selectedStr = moodArray[position];
                selectedMood = Mood.fromString(selectedStr);
            }

            boolean last7Days = checkLastWeek.isChecked();
            boolean within5km = checkWithin5km.isChecked();
            String word = editWord.getText().toString().trim();

            if (listener != null) {
                listener.onFilterSave(selectedMood, last7Days, within5km, word);
            }
            dialog.dismiss();
        });

        return dialog;
    }
}
