package com.example.baobook;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.Privacy;
import com.example.baobook.model.SocialSetting;
import com.example.baobook.util.MoodUtils;
import com.example.baobook.util.LocationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/*
Edits current mood activity. Changes can be applied to trigger, mood, time, date, social status, and privacy.
*/

public class EditFragment extends DialogFragment {
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private EditMoodEventDialogListener listener;
    private MoodEvent moodEvent;
    private Location userLocation;
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    public EditFragment(MoodEvent clickedMood) {
        this.moodEvent = clickedMood;
        OffsetDateTime dateTime = clickedMood.getDateTime();
        this.selectedDate = dateTime.toLocalDate();
        this.selectedTime = dateTime.toLocalTime();
    }

    public EditFragment() {
        super();
    }

    public interface EditMoodEventDialogListener {
        void onMoodEdited(MoodEvent updatedMoodEvent);
    }

    public static boolean isValidDescription(String desc) {
        if (desc.isEmpty()) return true;
        return desc.length() <= 200;
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // default to first item if not found
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditMoodEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement EditMoodEventDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Fragment not attached to a context.");
        }

        View view = LayoutInflater.from(context).inflate(R.layout.edit_fragment, null);
        Spinner editMood = view.findViewById(R.id.edit_mood_spinner);
        TextView editDate = view.findViewById(R.id.text_date);
        TextView editTime = view.findViewById(R.id.text_time);
        EditText editDescription = view.findViewById(R.id.edit_description);
        Spinner editSocial = view.findViewById(R.id.social_spinner);
        SwitchCompat privacySwitch = view.findViewById(R.id.privacySwitch);
        SwitchCompat locationSwitch = view.findViewById(R.id.locationSwitch);

        // Initialize the Spinner with MoodUtils
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(
                context,
                android.R.layout.simple_spinner_item,
                Arrays.asList(MoodUtils.MOOD_OPTIONS),
                MoodUtils.MOOD_COLORS,
                MoodUtils.MOOD_EMOJIS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editMood.setAdapter(adapter);

        if (moodEvent != null) {
            int position = adapter.getPosition(moodEvent.getMood().toString());
            editMood.setSelection(position);
            OffsetDateTime dateTime = moodEvent.getDateTime();
            editDate.setText(dateFormat.format(dateTime));
            editTime.setText(timeFormat.format(dateTime));
            editDescription.setText(moodEvent.getDescription());
            privacySwitch.setChecked(moodEvent.getPrivacy() == Privacy.PRIVATE);
            int socialPosition = getSpinnerIndex(editSocial, moodEvent.getSocial().toString());
            editSocial.setSelection(socialPosition);
            locationSwitch.setChecked(moodEvent.getLocation() != null);

            if (moodEvent.getLocation() != null) {
                userLocation = new Location("");
                userLocation.setLatitude(moodEvent.getLocation().getLatitude());
                userLocation.setLongitude(moodEvent.getLocation().getLongitude());
            }
        }

        editDate.setOnClickListener(v -> {
            new DatePickerDialog(context,
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate = selectedDate
                                .withYear(year)
                                .withMonth(month)
                                .withDayOfMonth(dayOfMonth);
                        editDate.setText(dateFormat.format(selectedDate));
                    },
                    selectedDate.getYear(),
                    selectedDate.getMonth().getValue()-1,
                    selectedDate.getDayOfMonth()
            ).show();
        });

        editTime.setOnClickListener(v -> {
            new TimePickerDialog(context,
                    (view12, hourOfDay, minute) -> {
                        selectedTime.withHour(hourOfDay);
                        selectedTime.withMinute(minute);

                        editTime.setText(timeFormat.format(selectedTime));
                    },
                    selectedTime.getHour() % 12,
                    selectedTime.getMinute(),
                    true
            ).show();
        });

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                LocationHelper.getCurrentLocation(requireContext(), new LocationHelper.LocationResultCallback() {
                    @Override
                    public void onLocationResult(double latitude, double longitude) {
                        userLocation = new Location("");
                        userLocation.setLatitude(latitude);
                        userLocation.setLongitude(longitude);
                    }

                    @Override
                    public void onLocationError(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        locationSwitch.setChecked(false);
                    }
                });
            } else {
                userLocation = null;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Edit Mood Event")
                .setPositiveButton("Save", null) // Set to null for overriding later
                .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                Mood newMood = Mood.fromString(editMood.getSelectedItem().toString());
                String newDescription = editDescription.getText().toString().trim();
                SocialSetting newSocial = SocialSetting.fromString(editSocial.getSelectedItem().toString());
                OffsetDateTime dateTime = OffsetDateTime.of(selectedDate, selectedTime, ZoneOffset.UTC);
                Privacy newPrivacy = privacySwitch.isChecked() ? Privacy.PRIVATE : Privacy.PUBLIC;

                if (!isValidDescription(newDescription)) {
                    Toast.makeText(getContext(), "Trigger must be at most 200 chars", Toast.LENGTH_SHORT).show();
                    return; // Prevent dialog from closing
                }

                moodEvent.editMoodEvent(newMood, dateTime, newDescription, newSocial, newPrivacy);

                if (userLocation != null) {
                    moodEvent.setLocation(new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude()));
                } else {
                    moodEvent.setLocation(null);
                }

                listener.onMoodEdited(moodEvent);
                dialog.dismiss(); // Dismiss only when validation passes
            });
        });

        return dialog;
    }
    }
