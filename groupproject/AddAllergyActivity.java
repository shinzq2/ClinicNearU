package com.example.groupproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.app.DatePickerDialog;
import java.util.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class AddAllergyActivity extends AppCompatActivity {

    private EditText editMedicineName, editReaction, editDate;
    private ImageView imgPreview;
    private Button btnSave, btnUploadImage;

    private String imagePath = null;
    private Uri imageUri = null;
    private List<AllergyModel> allergyList;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        imageUri = selectedImage;
                        imagePath = selectedImage.toString();
                        imgPreview.setImageURI(selectedImage);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imagePath != null) {
                    imgPreview.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_allergy);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        editMedicineName = findViewById(R.id.editMedicineName);
        editReaction = findViewById(R.id.editReaction);
        editDate = findViewById(R.id.editDate);
        imgPreview = findViewById(R.id.imgPreview);
        btnSave = findViewById(R.id.btnSaveAllergy);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        allergyList = StorageHelper.getAllergyList(this);

        boolean isEdit = getIntent().getBooleanExtra("isEdit", false);
        String allergyId = getIntent().getStringExtra("allergyId");

        if (isEdit && allergyId != null) {
            for (AllergyModel item : allergyList) {
                if (item.getAllergyId().equals(allergyId)) {
                    editMedicineName.setText(item.getMedicineName());
                    editReaction.setText(item.getReaction());
                    editDate.setText(item.getDateNoted());
                    imagePath = item.getImagePath();
                    if (imagePath != null) {
                        if (imagePath.startsWith("content://")) {
                            imgPreview.setImageURI(Uri.parse(imagePath));
                        } else {
                            imgPreview.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                        }
                    }
                    break;
                }
            }
        }

        btnUploadImage.setOnClickListener(v -> {
            if (checkPermissions()) {
                showImagePickerDialog();
            } else {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_IMAGES
                }, 101);
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = editMedicineName.getText().toString().trim();
            String reaction = editReaction.getText().toString().trim();
            String date = editDate.getText().toString().trim();

            if (name.isEmpty() || reaction.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit && allergyId != null) {
                for (AllergyModel item : allergyList) {
                    if (item.getAllergyId().equals(allergyId)) {
                        item.setMedicineName(name);
                        item.setReaction(reaction);
                        item.setDateNoted(date);
                        item.setImagePath(imagePath);
                        updateAllergyInFirebase(user.getUid(), item);
                        break;
                    }
                }
            } else {
                String id = UUID.randomUUID().toString();
                AllergyModel newAllergy = new AllergyModel(id, name, reaction, date, imagePath);
                allergyList.add(newAllergy);
                saveAllergyToFirebase(user.getUid(), newAllergy);
            }

            StorageHelper.saveAllergyList(this, allergyList);
            Toast.makeText(this, "Allergy saved", Toast.LENGTH_SHORT).show();
            finish();
        });

        editDate = findViewById(R.id.editDate);

        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        editDate.setText(formattedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });


    }

    private boolean checkPermissions() {
        boolean cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean readStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean readMedia = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        return cam && readStorage && readMedia;
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Medicine Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            imageUri = photoURI;
            cameraLauncher.launch(intent);
        }
    }

    private File createImageFile() {
        try {
            String fileName = "IMG_" + System.currentTimeMillis();
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = new File(storageDir, fileName + ".jpg");
            imagePath = image.getAbsolutePath();
            return image;
        } catch (Exception e) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void saveAllergyToFirebase(String userId, AllergyModel allergy) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("allergies")
                .child(userId)
                .child(allergy.getAllergyId());
        ref.setValue(allergy);
    }

    private void updateAllergyInFirebase(String userId, AllergyModel allergy) {
        saveAllergyToFirebase(userId, allergy);
    }
}
