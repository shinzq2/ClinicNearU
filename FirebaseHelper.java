package com.example.groupproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    // Simpan atau update allergy ke Firebase
    public static void saveAllergyToFirebase(AllergyModel model) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("allergyData")
                .child(userId)
                .child(model.getAllergyId());

        // Hantar data ke Firebase tanpa imagePath
        AllergyModel allergy = new AllergyModel(
                model.getAllergyId(),
                model.getMedicineName(),
                model.getReaction(),
                model.getDateNoted(),
                null
        );

        ref.setValue(allergy);
    }

    // Padam allergy dari Firebase
    public static void deleteAllergyFromFirebase(String allergyId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("allergyData")
                .child(userId)
                .child(allergyId);

        ref.removeValue();
    }
}
