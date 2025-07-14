package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class EditProfileActivity extends AppCompatActivity {

    EditText name, email, phone, ic, age, address, emerName, emerPhone, emerRelation;
    Spinner genderSpinner;
    RadioGroup bloodGroup;
    RadioButton bloodA, bloodB, bloodAB, bloodO;
    Button btnSave;

    FirebaseAuth auth;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Link UI
        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.profile_email);
        phone = findViewById(R.id.profile_phone);
        ic = findViewById(R.id.profile_ic);
        age = findViewById(R.id.profile_age);
        address = findViewById(R.id.profile_address);
        emerName = findViewById(R.id.profile_emergency_name);
        emerPhone = findViewById(R.id.profile_emergency_phone);
        emerRelation = findViewById(R.id.profile_emergency_relationship);
        genderSpinner = findViewById(R.id.profile_gender);
        bloodGroup = findViewById(R.id.profile_blood);
        bloodA = findViewById(R.id.blood_a);
        bloodB = findViewById(R.id.blood_b);
        bloodAB = findViewById(R.id.blood_ab);
        bloodO = findViewById(R.id.blood_o);
        btnSave = findViewById(R.id.btn_save_profile);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid());

        // Load user data
        db.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                name.setText(snapshot.child("name").getValue(String.class));
                email.setText(snapshot.child("email").getValue(String.class));
                phone.setText(snapshot.child("phone").getValue(String.class));
                ic.setText(snapshot.child("ic").getValue(String.class));
                age.setText(snapshot.child("age").getValue(String.class));
                address.setText(snapshot.child("address").getValue(String.class));
                emerName.setText(snapshot.child("emergencyName").getValue(String.class));
                emerPhone.setText(snapshot.child("emergencyPhone").getValue(String.class));
                emerRelation.setText(snapshot.child("relationship").getValue(String.class));

                String genderVal = snapshot.child("gender").getValue(String.class);
                if (genderVal != null) {
                    int pos = adapter.getPosition(genderVal);
                    genderSpinner.setSelection(pos);
                }

                String blood = snapshot.child("bloodType").getValue(String.class);
                if (blood != null) {
                    switch (blood) {
                        case "A": bloodA.setChecked(true); break;
                        case "B": bloodB.setChecked(true); break;
                        case "AB": bloodAB.setChecked(true); break;
                        case "O": bloodO.setChecked(true); break;
                    }
                }
            }
        });

        // Save button
        btnSave.setOnClickListener(v -> {
            if (name.getText().toString().isEmpty() ||
                    email.getText().toString().isEmpty() ||
                    phone.getText().toString().isEmpty() ||
                    ic.getText().toString().isEmpty() ||
                    age.getText().toString().isEmpty() ||
                    emerPhone.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ic.getText().toString().matches("\\d{6}-\\d{2}-\\d{4}")) {
                Toast.makeText(this, "IC must be in format XXXXXX-XX-XXXX", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.getText().toString().matches("01\\d{8,9}")) {
                Toast.makeText(this, "Phone number must be valid (e.g., 0123456789)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!emerPhone.getText().toString().matches("01\\d{8,9}")) {
                Toast.makeText(this, "Emergency phone number must be valid", Toast.LENGTH_SHORT).show();
                return;
            }


            String selectedGender = genderSpinner.getSelectedItem().toString();
            int selectedBloodId = bloodGroup.getCheckedRadioButtonId();
            RadioButton selectedBlood = findViewById(selectedBloodId);
            String bloodType = (selectedBlood != null) ? selectedBlood.getText().toString() : "";

            db.child("name").setValue(name.getText().toString());
            db.child("email").setValue(email.getText().toString());
            db.child("phone").setValue(phone.getText().toString());
            db.child("ic").setValue(ic.getText().toString());
            db.child("age").setValue(age.getText().toString());
            db.child("address").setValue(address.getText().toString());
            db.child("emergencyName").setValue(emerName.getText().toString());
            db.child("emergencyPhone").setValue(emerPhone.getText().toString());
            db.child("relationship").setValue(emerRelation.getText().toString());
            db.child("gender").setValue(selectedGender);
            db.child("bloodType").setValue(bloodType);

            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(EditProfileActivity.this, ViewProfileActivity.class));
            finish();
        });
    }
}
