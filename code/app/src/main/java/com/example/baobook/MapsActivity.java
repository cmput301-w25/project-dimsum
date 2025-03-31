package com.example.baobook;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodFilterState;
import com.example.baobook.util.MoodUtils;
import com.example.baobook.util.UserSession;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.baobook.databinding.ActivityMapsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
//  This activity displays a Google Map with mood event markers submitted by the user and people they follow.
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, MapFilterDialogFragment.OnFilterSaveListener,
        MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener {

    private final MoodEventHelper moodEventHelper = new MoodEventHelper();
    private UserSession userSession;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LinearLayout activeFiltersContainer;
    private final ArrayList<MoodEvent> allMoodEvents = new ArrayList<>();
    private final ArrayList<MoodEvent> filteredList = new ArrayList<>();
    private final MoodFilterState filterState = new MoodFilterState();
    // We'll display the "filtered" results in memory, for the ListView
    // So we keep a local list for quick adaptation to the UI
    private final ArrayList<Marker> moodMarkers = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final ActivityResultLauncher<Intent> addMoodLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            MoodEvent mood = (MoodEvent) result.getData().getParcelableExtra("moodEvent");
                            if (mood != null) {
                                Toast.makeText(this, "Mood added!", Toast.LENGTH_SHORT).show();
                                allMoodEvents.add(0, mood);
                                filteredList.add(0, mood);
                                renderMoodEventsOnMap();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSession = new UserSession(this);

        com.example.baobook.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        Button openFilterButton = findViewById(R.id.open_filter_button);
        Button clearAllButton = findViewById(R.id.clear_all_button);
        activeFiltersContainer = findViewById(R.id.active_filters_container);
        FloatingActionButton addButton = findViewById(R.id.add_button);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //hide status bar at the top
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Bottom nav
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, Home.class);
            startActivity(intent);
        });
        // Add Button
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, AddMoodActivity.class);
            addMoodLauncher.launch(intent);
        });
        // Profile button
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
        // Filter button -> open dialog
        openFilterButton.setOnClickListener(v -> {
            MapFilterDialogFragment dialog = new MapFilterDialogFragment(this);
            dialog.setExistingFilters(filterState.getMood(), filterState.isRecentWeek(), filterState.isWithin5km(), filterState.getWord());
            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });
        // Clear All -> remove filters
        clearAllButton.setOnClickListener(v -> {
            filterState.clear();
            applyFilters();
            Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom options
        mMap.getUiSettings().setZoomControlsEnabled(true);   // Show +/- buttons
        mMap.getUiSettings().setZoomGesturesEnabled(true);   // Allow pinch-to-zoom

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            centerMapOnUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Load mood events from firestore and render them on the map.
        loadMoodEvents();
        mMap.setOnMarkerClickListener(marker -> {
            MoodEvent event = (MoodEvent) marker.getTag();

            if (event != null) {
                // If the event is authored by the current user, use the editable mood event details fragment.
                if (event.getUsername().equals(userSession.getUsername())) {
                    MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(event);
                    fragment.show(getSupportFragmentManager(), "MoodDetailsEditable");
                }

                // Else, use the non-editable one.
                else {
                    MoodEventDetailsFragment fragment = new MoodEventDetailsFragment(event);
                    fragment.show(getSupportFragmentManager(), "MoodDetails");
                }
            }
            return true; // prevent default info window
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable location layer and center map
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                        centerMapOnUserLocation();
                    }
                }
            } else {
                // Permission denied, show a message or disable location features
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMoodEvents() {
        CompletableFuture<List<MoodEvent>> userMoodEvents = new CompletableFuture<>();
        CompletableFuture<List<MoodEvent>> followingMoodEvents = new CompletableFuture<>();

        moodEventHelper.getMoodEventsByUser(userSession.getUsername(),
                userMoodEvents::complete,
                userMoodEvents::completeExceptionally
        );
        moodEventHelper.getRecentFollowingMoodEvents(userSession.getUsername(),
                followingMoodEvents::complete,
                followingMoodEvents::completeExceptionally
        );

        CompletableFuture
                .allOf(userMoodEvents, followingMoodEvents)
                .thenRun(() -> {
                    try {
                        List<MoodEvent> userEvents = userMoodEvents.get();
                        allMoodEvents.addAll(userEvents);
                        filteredList.addAll(userEvents);
                    } catch (Exception e) {
                        Log.e("Firestore", "Error loading your own moods", e);
                        runOnUiThread(() ->
                                Toast.makeText(this, "Failed to load your own moods.", Toast.LENGTH_SHORT).show()
                        );
                    }

                    try {
                        List<MoodEvent> followingEvents = followingMoodEvents.get();
                        allMoodEvents.addAll(followingEvents);
                        filteredList.addAll(followingEvents);
                    } catch (Exception e) {
                        Log.e("Firestore", "Error loading following moods", e);
                        runOnUiThread(() ->
                                Toast.makeText(this, "Failed to load following moods.", Toast.LENGTH_SHORT).show()
                        );
                    }
                    runOnUiThread(this::renderMoodEventsOnMap);
                });
    }

    @Override
    public void onEditMoodEvent(MoodEvent mood) {
        EditFragment fragment = new EditFragment(mood);
        fragment.show(getSupportFragmentManager(), "Edit Mood");
    }

    @Override
    public void onDeleteMoodEvent(MoodEvent mood) {
        moodEventHelper.deleteMood(mood, aVoid -> {
            allMoodEvents.remove(mood);  // Remove the mood event from cache.
            filteredList.remove(mood);  // Remove it from the filtered list if it is present.
            renderMoodEventsOnMap();
            Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show();
        }, e -> {
            Log.e("Firestore", "Error deleting mood", e);
            Toast.makeText(this, "Failed to delete mood.", Toast.LENGTH_SHORT).show();
        });
    }
    private void centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Use the new Priority-based API (non-deprecated)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null && mMap != null) {
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            currentLocation = location;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                        } else {
                            // Fallback if location is null (e.g., GPS off, no recent fix)
                            Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
                            // Optionally set a default location (e.g., last known city)
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MapActivity", "Location error", e);
                        Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    /**
     * Gets a the filtered list of MoodEvents, renders the MoodEvents on the map,
     * and rebuilds the filter “chips”.
     */
    private void applyFilters() {
        ArrayList<MoodEvent> moodEvents = filterState.applyFilters(allMoodEvents, currentLocation);

        filteredList.clear();
        filteredList.addAll(moodEvents);

        renderMoodEventsOnMap();
        rebuildActiveFiltersChips();
    }

    private void renderMoodEventsOnMap() {
        for (Marker m : moodMarkers) {
            m.remove();
        }
        moodMarkers.clear();

        for (MoodEvent moodEvent : filteredList) {
            addMarker(moodEvent);
        }
    }

    private void renderMoodEventOnMap(MoodEvent moodEvent) {
        for (Marker m : moodMarkers) {
            if (m.getTag() == moodEvent) {
                m.remove();
            }
        }

        addMarker(moodEvent);
    }

    private void addMarker(MoodEvent moodEvent) {
        if (moodEvent.getLocation() != null) {  // Todo: remove this condition when all MoodEvents have locations.
            GeoPoint geoPoint = moodEvent.getLocation();
            if (geoPoint != null) {
                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(createMoodMarker(moodEvent))
                        .anchor(0.5f, 1f));  // Translate the marker so that it points to the accurate location.

                if (marker != null) {
                    marker.setTag(moodEvent);  // Associate the marker with the MoodEvent.
                    marker.showInfoWindow();
                }
                moodMarkers.add(marker);
            }
        }
    }

    // Show “chips” for each active filter
    private void rebuildActiveFiltersChips() {
        Mood mood = filterState.getMood();
        boolean isRecentWeek = filterState.isRecentWeek();
        boolean isWithin5km = filterState.isWithin5km();
        String word = filterState.getWord();

        activeFiltersContainer.removeAllViews();

        if (mood != null) {
            activeFiltersContainer.addView(
                    createChip(mood.toString(), v -> {
                        filterState.setMood(null);
                        applyFilters();
                    })
            );
        }

        if (isRecentWeek) {
            activeFiltersContainer.addView(
                    createChip("Last 7 days", v -> {
                        filterState.setRecentWeek(false);
                        applyFilters();
                    })
            );
        }

        if (isWithin5km) {
            activeFiltersContainer.addView(
                    createChip("Within 5 km", v -> {
                        filterState.setWithin5km(false);
                        applyFilters();
                    })
            );
        }

        if (word != null && !word.isEmpty()) {
            activeFiltersContainer.addView(
                    createChip("Word: " + word, v -> {
                        filterState.setWord(null);
                        applyFilters();
                    })
            );
        }
    }

    private View createChip(String text, View.OnClickListener onRemove) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView txt = new TextView(this);
        txt.setText(text + "  ");
        layout.addView(txt);

        TextView x = new TextView(this);
        x.setText("X");
        x.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        x.setOnClickListener(onRemove);
        layout.addView(x);

        layout.setPadding(16, 8, 16, 8);
        return layout;
    }

    @Override
    public void onFilterSave(Mood mood, boolean lastWeek, boolean within5km, String word) {
        filterState.setMood(mood);
        filterState.setRecentWeek(lastWeek);
        filterState.setWithin5km(within5km);
        filterState.setWord(word);
        applyFilters();
    }

    private BitmapDescriptor createMoodMarker(MoodEvent moodEvent) {
        String text = String.format("%s: @%s",
                moodEvent.getMood().toString(),
                moodEvent.getUsername());

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.LEFT);

        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        int padding = 20;
        int pointerHeight = 20;
        int width = textBounds.width() + padding * 2;
        int height = textBounds.height() + padding * 2 + pointerHeight;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(MoodUtils.getMoodColor(moodEvent.getMood().toString()));

        // Draw the rounded bubble (excluding pointer)
        RectF bubbleRect = new RectF(0, 0, width, height - pointerHeight);
        canvas.drawRoundRect(bubbleRect, 30, 30, bgPaint);

        // Draw the little triangle pointer
        Path pointer = new Path();
        float centerX = width / 2f;
        pointer.moveTo(centerX - 20, height - pointerHeight);
        pointer.lineTo(centerX + 20, height - pointerHeight);
        pointer.lineTo(centerX, height);
        pointer.close();
        canvas.drawPath(pointer, bgPaint);

        // Draw the text
        float x = padding;
        float y = padding - textBounds.top; // aligns text vertically
        canvas.drawText(text, x, y, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
        moodEventHelper.updateMood(updatedMoodEvent, aVoid -> {
                    Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show();

                    // Update the cache.
                    for (MoodEvent moodEvent : allMoodEvents) {
                        if (moodEvent.getId().equals(updatedMoodEvent.getId())) {
                            moodEvent.updateMoodEvent(updatedMoodEvent);
                        }
                    }
                    for (MoodEvent moodEvent : filteredList) {
                        if (moodEvent.getId().equals(updatedMoodEvent.getId())) {
                            moodEvent.updateMoodEvent(updatedMoodEvent);
                        }
                    }

                    // Refresh the map marker.
                    renderMoodEventOnMap(updatedMoodEvent);
                }, e -> {
                    Log.e("Firestore", "Error updating mood", e);
                    Toast.makeText(this, "Failed to update mood.", Toast.LENGTH_SHORT).show();
        });
    }
}
