package com.example.groupproject;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ClinicDetailActivity extends AppCompatActivity {

    TextView detailTitle, detailDoctor, detailPhone, detailAddress, detailHours;
    ImageView detailImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_detail);

        detailTitle = findViewById(R.id.detail_title);
        detailDoctor = findViewById(R.id.detail_doctor);
        detailPhone = findViewById(R.id.detail_phone);
        detailAddress = findViewById(R.id.detail_address);
        detailHours = findViewById(R.id.detail_hours);
        detailImage = findViewById(R.id.imageView_id);

        detailTitle.setText(getIntent().getStringExtra("title"));
        detailDoctor.setText("Doctor: " + getIntent().getStringExtra("doctor"));
        detailPhone.setText("Phone: " + getIntent().getStringExtra("phone"));
        detailAddress.setText("Address: " + getIntent().getStringExtra("address"));
        detailHours.setText("Operating Hours: " + getIntent().getStringExtra("hours"));
        detailImage.setImageResource(getIntent().getIntExtra("image", R.drawable.clinic1));
    }
}
