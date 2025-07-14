package com.example.groupproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class NearbyClinicActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 1001;
    private static final String API_KEY = "API_KEYS";

    private GoogleMap mMap;
    private LatLng currentLatLng;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText searchBar;
    private LinearLayout clinicListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_clinic);

        searchBar = findViewById(R.id.search_bar);
        clinicListLayout = findViewById(R.id.clinic_list_layout);
        ImageView searchIcon = findViewById(R.id.search_icon);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchIcon.setOnClickListener(v -> {
            String keyword = searchBar.getText().toString().trim();
            if (!keyword.isEmpty()) {
                performNearbySearch(keyword);
            } else {
                Toast.makeText(this, "Enter keyword like 'Clinic'", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_search); // Highlight current tab

        nav.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });
    }

    private void handleNavigation(int itemId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (itemId == R.id.nav_search) {
            return;
        }

        if (user == null) {
            showLoginDialog();
            return;
        }

        Intent intent = null;

        if (itemId == R.id.nav_visit) {
            intent = new Intent(this, VisitHistoryActivity.class);
        } else if (itemId == R.id.nav_allergy) {
            intent = new Intent(this, MedicineAllergyActivity.class);
        } else if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ViewProfileActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Authentication Required")
                .setMessage("Please login or register first to use this feature.")
                .setPositiveButton("Login", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted yet
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
                performNearbySearch("clinic");
            } else {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void performNearbySearch(String keyword) {
        if (currentLatLng == null) {
            Toast.makeText(this, "Location not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // Valid keywords only
        String[] allowedKeywords = {"clinic", "klinik", "hospital", "pusat kesihatan"};
        boolean isValid = false;
        for (String allowed : allowedKeywords) {
            if (keyword.equalsIgnoreCase(allowed)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            Toast.makeText(this, "Search allowed: Klinik, Clinic, Hospital, Pusat Kesihatan only", Toast.LENGTH_LONG).show();
            return;
        }

        clinicListLayout.removeAllViews();
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));

        String encoded = Uri.encode(keyword);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                "&radius=10000&keyword=" + encoded +
                "&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray results = response.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                    String name = obj.getString("name");
                    String address = obj.optString("vicinity", "Address not found");

                    if (!name.toLowerCase().contains(keyword.toLowerCase())) {
                        continue; 
                    }

                    LatLng latLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                    MarkerOptions marker = new MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .snippet(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    mMap.addMarker(marker);
                    addClinicCard(name, address, latLng);
                }

            } catch (Exception e) {
                Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }


    private void addClinicCard(String name, String address, LatLng latLng) {
        CardView card = new CardView(this);
        card.setCardElevation(8);
        card.setRadius(24);
        card.setUseCompatPadding(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(getResources().getColor(android.R.color.white));

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(18);
        layout.addView(nameView);

        TextView addressView = new TextView(this);
        addressView.setText(address);
        addressView.setPadding(0, 16, 0, 0);
        layout.addView(addressView);

        card.addView(layout);
        clinicListLayout.addView(card);

        card.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latLng.latitude + "," + latLng.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    .setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) == null) {
                mapIntent.setPackage(null);
            }
            startActivity(mapIntent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
