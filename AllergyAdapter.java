package com.example.groupproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class AllergyAdapter extends ArrayAdapter<AllergyModel> {

    private Context context;
    private List<AllergyModel> list;

    public AllergyAdapter(Context context, List<AllergyModel> list) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_allergy, parent, false);

        AllergyModel allergy = list.get(position);

        TextView tvName = convertView.findViewById(R.id.tvMedicineName);
        TextView tvReaction = convertView.findViewById(R.id.tvReaction);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        ImageView imgMedicine = convertView.findViewById(R.id.imgMedicine);

        tvName.setText(allergy.getMedicineName());
        tvReaction.setText("Reaction: " + allergy.getReaction());
        tvDate.setText("Noted on: " + allergy.getDateNoted());

        String imagePath = allergy.getImagePath();

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                if (imagePath.startsWith("content://")) {
                    // Gallery image (content URI)
                    InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(imagePath));
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imgMedicine.setImageBitmap(bitmap);
                        inputStream.close();
                    } else {
                        imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
                    }
                } else {
                    // Camera image (file path)
                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        imgMedicine.setImageBitmap(bitmap);
                    } else {
                        imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
            }
        } else {
            // No imagePath provided — fallback
            imgMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
        }

        return convertView;
    }
}
