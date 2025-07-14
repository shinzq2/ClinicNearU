package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passInput, confirmInput, phoneInput, icInput, ageInput;
    Button registerBtn;

    FirebaseAuth auth;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.reg_name);
        emailInput = findViewById(R.id.reg_email);
        passInput = findViewById(R.id.reg_password);
        confirmInput = findViewById(R.id.reg_confirm);
        phoneInput = findViewById(R.id.reg_phone);
        icInput = findViewById(R.id.reg_ic);
        ageInput = findViewById(R.id.reg_age);
        registerBtn = findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String pass = passInput.getText().toString().trim();
            String confirm = confirmInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String ic = icInput.getText().toString().trim();
            String age = ageInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()
                    || phone.isEmpty() || ic.isEmpty() || age.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if (pass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!ic.matches("\\d{6}-\\d{2}-\\d{4}")) {
                Toast.makeText(this, "IC format invalid. Example: 990101-01-1234", Toast.LENGTH_SHORT).show();
            } else {
                auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = auth.getCurrentUser().getUid();

                                HashMap<String, String> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("phone", phone);
                                userData.put("ic", ic);
                                userData.put("age", age);

                                db.child(uid).setValue(userData)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Toast.makeText(this, "Register failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
