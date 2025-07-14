package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.UUID;

public class ScanQRActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference visitRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        visitRef = FirebaseDatabase.getInstance().getReference("visitHistory").child(uid);

        // Launch QR scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan Clinic QR Code");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                parseAndCheckDuplicate(result.getContents());
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                finish(); // Close scanner
            }
        }
    }

    private void parseAndCheckDuplicate(String qrText) {
        // Parse QR content
        HashMap<String, String> visitData = new HashMap<>();
        String[] lines = qrText.split("\n");

        for (String line : lines) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                visitData.put(parts[0].trim(), parts[1].trim());
            }
        }

        String clinicName = visitData.get("Clinic Name");
        String date = visitData.get("Date");
        String time = visitData.get("Time");

        if (clinicName == null || date == null || time == null) {
            Toast.makeText(this, "Invalid QR format", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if same Clinic Name + Date + Time already exists
        visitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean isDuplicate = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot visitSnap : snapshot.getChildren()) {
                    String existingClinic = visitSnap.child("Clinic Name").getValue(String.class);
                    String existingDate = visitSnap.child("Date").getValue(String.class);
                    String existingTime = visitSnap.child("Time").getValue(String.class);

                    if (clinicName.equals(existingClinic) && date.equals(existingDate) && time.equals(existingTime)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    Toast.makeText(ScanQRActivity.this, "Visit already exists", Toast.LENGTH_LONG).show();
                    finish(); // Return without saving
                } else {
                    // No duplicate found, proceed to save
                    String visitId = UUID.randomUUID().toString();
                    visitRef.child(visitId).setValue(visitData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ScanQRActivity.this, "Visit saved", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ScanQRActivity.this, VisitHistoryActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ScanQRActivity.this, "Failed to save visit", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScanQRActivity.this, "Error checking for duplicates", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
