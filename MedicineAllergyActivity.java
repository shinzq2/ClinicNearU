package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MedicineAllergyActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnAdd;
    private List<AllergyModel> allergyList;
    private AllergyAdapter adapter;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_allergy);

        listView = findViewById(R.id.listViewAllergy);
        btnAdd = findViewById(R.id.btnAddAllergy);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        allergyList = new ArrayList<>();
        adapter = new AllergyAdapter(this, allergyList);
        listView.setAdapter(adapter);

        // Firebase reference
        dbRef = FirebaseDatabase.getInstance()
                .getReference("allergies")
                .child(currentUser.getUid());

        fetchAllergyFromFirebase();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AllergyModel model = allergyList.get(position);
            Intent intent = new Intent(this, AddAllergyActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("allergyId", model.getAllergyId());
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            AllergyModel model = allergyList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Allergy")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbRef.child(model.getAllergyId()).removeValue();
                        allergyList.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddAllergyActivity.class));
        });

        setupBottomNavigation();
    }

    private void fetchAllergyFromFirebase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allergyList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    AllergyModel model = item.getValue(AllergyModel.class);
                    if (model != null) {
                        // Load image path locally if exists
                        List<AllergyModel> localList = StorageHelper.getAllergyList(MedicineAllergyActivity.this);
                        for (AllergyModel localItem : localList) {
                            if (localItem.getAllergyId().equals(model.getAllergyId())) {
                                model.setImagePath(localItem.getImagePath());
                                break;
                            }
                        }
                        allergyList.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MedicineAllergyActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_allergy);

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
