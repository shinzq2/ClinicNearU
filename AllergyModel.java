package com.example.groupproject;

public class AllergyModel {
    private String allergyId;
    private String medicineName;
    private String reaction;
    private String dateNoted;
    private String imagePath; // Store local image path

    // Default constructor (required for Firebase)
    public AllergyModel() {}

    // Constructor with all fields
    public AllergyModel(String allergyId, String medicineName, String reaction, String dateNoted, String imagePath) {
        this.allergyId = allergyId;
        this.medicineName = medicineName;
        this.reaction = reaction;
        this.dateNoted = dateNoted;
        this.imagePath = imagePath;
    }

    // Getters
    public String getAllergyId() { return allergyId; }
    public String getMedicineName() { return medicineName; }
    public String getReaction() { return reaction; }
    public String getDateNoted() { return dateNoted; }
    public String getImagePath() { return imagePath; }

    // Setters
    public void setAllergyId(String allergyId) { this.allergyId = allergyId; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setReaction(String reaction) { this.reaction = reaction; }
    public void setDateNoted(String dateNoted) { this.dateNoted = dateNoted; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllergyModel)) return false;
        AllergyModel that = (AllergyModel) o;
        return allergyId.equals(that.allergyId);
    }
}
