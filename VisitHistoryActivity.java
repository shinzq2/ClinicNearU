package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class VisitHistoryActivity extends AppCompatActivity {

    ListView visitListView;
    Button btnAddVisit;

    ArrayList<String> clinicDisplayList = new ArrayList<>();
    ArrayList<String> clinicNameKeys = new ArrayList<>();
    ArrayAdapter<String> adapter;

    DatabaseReference visitRef;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_history);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        visitListView = findViewById(R.id.visit_list);
        btnAddVisit = findViewById(R.id.btn_add_visit);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        visitRef = FirebaseDatabase.getInstance().getReference("visitHistory").child(userId);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, clinicDisplayList);
        visitListView.setAdapter(adapter);

        loadVisitHistory();

        visitListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedClinic = clinicNameKeys.get(position);
            Intent intent = new Intent(this, VisitDetailActivity.class);
            intent.putExtra("clinicName", selectedClinic);
            startActivity(intent);
        });

        btnAddVisit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanQRActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    private void loadVisitHistory() {
        visitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clinicDisplayList.clear();
                clinicNameKeys.clear();

                HashMap<String, Integer> clinicCounts = new HashMap<>();

                for (DataSnapshot visitSnap : snapshot.getChildren()) {
                    String clinicName = visitSnap.child("Clinic Name").getValue(String.class);
                    if (clinicName != null) {
                        clinicCounts.put(clinicName, clinicCounts.getOrDefault(clinicName, 0) + 1);
                    }
                }

                for (Map.Entry<String, Integer> entry : clinicCounts.entrySet()) {
                    String name = entry.getKey();
                    int count = entry.getValue();
                    clinicNameKeys.add(name);
                    clinicDisplayList.add(name + " (" + count + " visit" + (count > 1 ? "s" : "") + ")");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VisitHistoryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_visit);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (id == R.id.nav_search) {
                startActivity(new Intent(this, NearbyClinicActivity.class));
                return true;
            }

            if (user == null) {
                showLoginDialog();
                return false;
            }

            if (id == R.id.nav_visit) {
                return true;
            } else if (id == R.id.nav_allergy) {
                startActivity(new Intent(this, MedicineAllergyActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ViewProfileActivity.class));
                return true;
            }

            return false;
        });
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
}
