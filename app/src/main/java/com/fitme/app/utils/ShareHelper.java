package com.fitme.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fitme.app.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShareHelper {

    public static void shareBmiResult(
            Context context,
            float bmi,
            String status,
            int age,
            boolean isMale) {

        String ageLabel = String.valueOf(age);
        String genderLabel = isMale ? context.getString(R.string.gender_male) : context.getString(R.string.gender_female);

        String dateLabel = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date());
        String yearLabel = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

        String bmiFormatted = String.format(Locale.US, "%.1f", bmi);
        String shareText = context.getString(R.string.share_text_template, bmiFormatted, status);

        View shareView = LayoutInflater.from(context).inflate(R.layout.layout_share_card, null);

        TextView tvBmiValue = shareView.findViewById(R.id.shareBmiValue);
        TextView tvStatus   = shareView.findViewById(R.id.shareBmiStatus);
        TextView tvAge      = shareView.findViewById(R.id.shareAge);
        TextView tvGender   = shareView.findViewById(R.id.shareGender);
        TextView tvDate     = shareView.findViewById(R.id.shareDate);
        TextView tvYear     = shareView.findViewById(R.id.shareYear);

        tvBmiValue.setText(bmiFormatted);
        tvStatus.setText(status);
        tvAge.setText(ageLabel);
        tvGender.setText(genderLabel);
        tvDate.setText(dateLabel);
        if (tvYear != null) tvYear.setText(yearLabel);

        int badgeColor;
        if      (bmi < 18.5f) badgeColor = ContextCompat.getColor(context, R.color.bmi_underweight);
        else if (bmi < 25.0f) badgeColor = ContextCompat.getColor(context, R.color.bmi_normal);
        else if (bmi < 30.0f) badgeColor = ContextCompat.getColor(context, R.color.bmi_overweight);
        else                  badgeColor = ContextCompat.getColor(context, R.color.bmi_obese);

        if (tvStatus.getBackground() != null) {
            tvStatus.getBackground().setTint(badgeColor);
        }

        int width = 900;
        shareView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        shareView.layout(0, 0, shareView.getMeasuredWidth(), shareView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(
                shareView.getMeasuredWidth(),
                shareView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        shareView.draw(canvas);

        try {
            File cacheDir = new File(context.getCacheDir(), "shared_images");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, "fitme_bmi_result.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // ✅ PERBAIKAN FATAL: Menyamakan akhiran dengan AndroidManifest (.fileprovider)
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}