package com.example.groupproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class VisitDetailActivity extends AppCompatActivity {

    LinearLayout visitContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_detail);

        visitContainer = findViewById(R.id.visit_container);
        String selectedClinicName = getIntent().getStringExtra("clinicName");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference visitRef = FirebaseDatabase.getInstance()
                .getReference("visitHistory")
                .child(userId);

        visitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot visitSnap : snapshot.getChildren()) {
                    String clinicName = visitSnap.child("Clinic Name").getValue(String.class);

                    if (clinicName != null && clinicName.equals(selectedClinicName)) {
                        found = true;
                        View visitView = LayoutInflater.from(VisitDetailActivity.this)
                                .inflate(R.layout.item_visit_detail, visitContainer, false);

                        ((TextView) visitView.findViewById(R.id.txt_clinic_name)).setText(clinicName);
                        ((TextView) visitView.findViewById(R.id.txt_date)).setText(visitSnap.child("Date").getValue(String.class));
                        ((TextView) visitView.findViewById(R.id.txt_time)).setText(visitSnap.child("Time").getValue(String.class));
                        ((TextView) visitView.findViewById(R.id.txt_doctor)).setText(visitSnap.child("Doctor").getValue(String.class));
                        ((TextView) visitView.findViewById(R.id.txt_reason)).setText(visitSnap.child("Reason").getValue(String.class));
                        ((TextView) visitView.findViewById(R.id.txt_prescriptions)).setText(visitSnap.child("Prescriptions").getValue(String.class));
                        ((TextView) visitView.findViewById(R.id.txt_notes)).setText(visitSnap.child("Notes").getValue(String.class));

                        visitContainer.addView(visitView);
                    }
                }

                if (!found) {
                    Toast.makeText(VisitDetailActivity.this, "No visits found for this clinic", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VisitDetailActivity.this, "Failed to load visit details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
