package com.example.groupproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ViewProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference db;

    Button btnEdit, btnLogout;

    TextView txtName, txtEmail, txtGender, txtPhone, txtIC, txtAge, txtBlood,
            txtAddress, txtEmerName, txtEmerPhone, txtEmerRelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Link views
        btnEdit = findViewById(R.id.btn_edit);
        btnLogout = findViewById(R.id.btn_logout);
        txtName = findViewById(R.id.txt_name);
        txtEmail = findViewById(R.id.txt_email);
        txtGender = findViewById(R.id.txt_gender);
        txtPhone = findViewById(R.id.txt_phone);
        txtIC = findViewById(R.id.txt_ic);
        txtAge = findViewById(R.id.txt_age);
        txtBlood = findViewById(R.id.txt_blood);
        txtAddress = findViewById(R.id.txt_address);
        txtEmerName = findViewById(R.id.txt_emergency_name);
        txtEmerPhone = findViewById(R.id.txt_emergency_phone);
        txtEmerRelation = findViewById(R.id.txt_emergency_relationship);

        // Load user data
        db.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                txtName.setText("Full Name: " + snapshot.child("name").getValue(String.class));
                txtEmail.setText("Email: " + snapshot.child("email").getValue(String.class));
                txtGender.setText("Gender: " + snapshot.child("gender").getValue(String.class));
                txtPhone.setText("Phone: " + snapshot.child("phone").getValue(String.class));
                txtIC.setText("IC: " + snapshot.child("ic").getValue(String.class));
                txtAge.setText("Age: " + snapshot.child("age").getValue(String.class));
                txtBlood.setText("Blood Type: " + snapshot.child("bloodType").getValue(String.class));
                txtAddress.setText("Home Address: " + snapshot.child("address").getValue(String.class));
                txtEmerName.setText("Emergency Contact Name: " + snapshot.child("emergencyName").getValue(String.class));
                txtEmerPhone.setText("Emergency Phone: " + snapshot.child("emergencyPhone").getValue(String.class));
                txtEmerRelation.setText("Relationship: " + snapshot.child("relationship").getValue(String.class));
            }
        });

        // Buttons
        btnEdit.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ImageButton btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
        btnEmergencyCall.setOnClickListener(v -> {
            new AlertDialog.Builder(ViewProfileActivity.this)
                    .setTitle("Emergency Call")
                    .setMessage("Adakah anda pasti mahu menghubungi 999?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                        dialIntent.setData(Uri.parse("tel:999"));
                        startActivity(dialIntent);
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_profile);

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
                startActivity(new Intent(this, VisitHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_allergy) {
                startActivity(new Intent(this, MedicineAllergyActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
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
