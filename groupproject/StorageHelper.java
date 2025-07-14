package com.example.groupproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {
    private static final String PREF_NAME = "AllergyPrefs";
    private static final String KEY_ALLERGY_LIST = "allergy_list";

    public static void saveAllergyList(Context context, List<AllergyModel> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_ALLERGY_LIST, gson.toJson(list));
        editor.apply();
    }

    public static List<AllergyModel> getAllergyList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ALLERGY_LIST, null);
        if (json != null) {
            Type type = new TypeToken<List<AllergyModel>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }
}
