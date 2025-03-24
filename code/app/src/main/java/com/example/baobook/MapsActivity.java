package com.example.baobook;

import static com.example.baobook.MoodUtils.getMoodColor;
import static com.example.baobook.MoodUtils.getMoodEmoji;

import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baobook.controller.MoodEventHelper;
import com.example.baobook.model.Mood;
import com.example.baobook.model.MoodEvent;
import com.example.baobook.model.MoodFilterState;
import com.example.baobook.util.UserSession;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, FilterDialogFragment.OnFilterSaveListener,
        MoodEventOptionsFragment.MoodEventOptionsDialogListener,
        EditFragment.EditMoodEventDialogListener {

    private final MoodEventHelper moodEventHelper = new MoodEventHelper();
    private UserSession userSession;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout activeFiltersContainer;
    private final ArrayList<MoodEvent> filteredList = new ArrayList<>();
    private final MoodFilterState filterState = new MoodFilterState();
    // We'll display the "filtered" results in memory, for the ListView
    // So we keep a local list for quick adaptation to the UI
    private final ArrayList<Marker> moodMarkers = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSession = new UserSession(this);

        com.example.baobook.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        Button homeButton = findViewById(R.id.home_button);
//        Button mapButton = findViewById(R.id.map_button);
        Button profileButton = findViewById(R.id.profile_button);
        Button openFilterButton = findViewById(R.id.open_filter_button);
        Button clearAllButton = findViewById(R.id.clear_all_button);
        activeFiltersContainer = findViewById(R.id.active_filters_container);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Bottom nav
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, Home.class);
            startActivity(intent);
        });
        // Profile button
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
        // Filter button -> open dialog
        openFilterButton.setOnClickListener(v -> {
            FilterDialogFragment dialog = new FilterDialogFragment(this);
            dialog.setExistingFilters(filterState.getMood(), filterState.isRecentWeek(), filterState.getWord());
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            centerMapOnUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        loadMoodsFromFirestore();
        mMap.setOnMarkerClickListener(marker -> {
            MoodEvent event = (MoodEvent) marker.getTag();

            if (event != null) {
                // Todo: replace this with a MoodEventDetailsFragment (without editing options) because we will show following MoodEvents as well.
                MoodEventOptionsFragment fragment = new MoodEventOptionsFragment(event);

                fragment.show(getSupportFragmentManager(), "MoodDetails");
            }
            return true; // prevent default info window
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
            loadMoodsFromFirestore();
            Toast.makeText(this, "Mood deleted!", Toast.LENGTH_SHORT).show();
        }, e -> {
            Log.e("Firestore", "Error deleting mood", e);
            Toast.makeText(this, "Failed to delete mood.", Toast.LENGTH_SHORT).show();
        });
    }
    private void centerMapOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f));
                        Marker userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
                        if (userMarker != null) userMarker.showInfoWindow();
                    } else {
                        Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Gets a the filtered list of MoodEvents, renders the MoodEvents on the map,
     * and rebuilds the filter “chips”.
     */
    private void applyFilters() {
        ArrayList<MoodEvent> moodEvents = filterState.applyFilters();

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

        for (MoodEvent event : filteredList) {
            if (event.getLocation() != null) {  // Todo: remove this condition when all MoodEvents have locations.
                GeoPoint geoPoint = event.getLocation();
                if (geoPoint != null) {
                    LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(String.format("%s: @%s", event.getMood().toString(), event.getMood().toString()))
                            .icon(createMoodMarker(this, event)));

                    if (marker != null) {
                        marker.setTag(event);  // Associate the marker with the MoodEvent.
                        marker.showInfoWindow();
                    }
                    moodMarkers.add(marker);
                }
            }
        }
    }

    // Show “chips” for each active filter
    private void rebuildActiveFiltersChips() {
        Mood mood = filterState.getMood();
        boolean isRecentWeek = filterState.isRecentWeek();
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

    /**
     * Load from Firestore, clear the manager’s list, add each mood,
     * then show them in the UI with no filters initially.
     */
    private void loadMoodsFromFirestore() {
        moodEventHelper.getMoodEventsByUser(userSession.getUsername(), moodEvents -> {
            filteredList.clear();
            filteredList.addAll(moodEvents);
            renderMoodEventsOnMap();
        }, e -> {
            Log.e("Firestore", "Error loading moods", e);
            Toast.makeText(this, "Failed to load moods.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onFilterSave(Mood mood, boolean lastWeek, String word) {
        filterState.setMood(mood);
        filterState.setRecentWeek(lastWeek);
        filterState.setWord(word);
        applyFilters();
    }
    public static BitmapDescriptor createMoodMarker(Context context, MoodEvent moodEvent) {
        int size = 120;          // total size of the bitmap
        int borderWidth = 12;     // thickness of the border

        // Load and crop the profile image into a circle
        // Todo: load the moodEvent's author's profile picture here.
        Bitmap rawIcon = getBitmapFromVectorDrawable(context, R.drawable.default_profile_picture, size, size);
        Bitmap circularBitmap = getCircularBitmap(rawIcon, size - 2 * borderWidth);

        // Create output bitmap
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Draw the colored border
        Paint borderPaint = new Paint();
        borderPaint.setColor(getMoodColor(moodEvent.getMood().toString()));
        borderPaint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint);

        // Draw the circular image in the center
        canvas.drawBitmap(circularBitmap, borderWidth, borderWidth, null);

        return BitmapDescriptorFactory.fromBitmap(output);
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap, int diameter) {
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        float radius = diameter / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, diameter, diameter, false);
        canvas.drawBitmap(scaled, 0, 0, paint);

        return output;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onMoodEdited(MoodEvent updatedMoodEvent) {
        moodEventHelper.updateMood(updatedMoodEvent, aVoid -> {
            Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show();
            loadMoodsFromFirestore();
        }, e -> {
            Log.e("Firestore", "Error updating mood", e);
            Toast.makeText(this, "Failed to update mood.", Toast.LENGTH_SHORT).show();
        });
    }
}