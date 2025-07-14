package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView signupLink;

    FirebaseAuth auth;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");

        emailInput = findViewById(R.id.login_email);
        passwordInput = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.btn_login);
        signupLink = findViewById(R.id.link_signup);

        loginBtn.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = auth.getCurrentUser().getUid();
                                db.child(uid).get().addOnSuccessListener(snapshot -> {
                                    if (snapshot.exists()) {
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, NearbyClinicActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "No user data found. Please register again.", Toast.LENGTH_LONG).show();
                                        auth.signOut(); // Logout if no user data
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Database check failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            } else {
                                Toast.makeText(this, "Login failed. Please register first.", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
