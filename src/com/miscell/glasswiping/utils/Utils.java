package com.miscell.glasswiping.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;
import com.miscell.glasswiping.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chenjishi on 15/3/11.
 */
public class Utils {
    public static final int REQUEST_CODE_GALLERY = 1;
    public static final int REQUEST_CODE_CAMERA = 2;

    public static void storeImage(Bitmap image, File pictureFile) {
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public static TextView generateTextView(Context context, String text, int color, float size) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

        return textView;
    }

    public static Bitmap scaleCenterCrop(Bitmap src, int newWidth, int newHeight) {
        int sourceWidth = src.getWidth();
        int sourceHeight = src.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, src.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(src, null, targetRect, null);

        return dest;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int resId) {
        showToast(context, context.getString(resId));
    }

    public static String getCurrentVersionName(Context context) {
        String versionName = "";
        Context appContext = context.getApplicationContext();

        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(appContext.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }


    public static synchronized boolean isNetworkConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo ni = connManager.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnected();
            }
        }
        return false;
    }

    public static void choosePhoto(final Context context, final Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(new CharSequence[]{context.getString(R.string.camera),
                        context.getString(R.string.photo_album)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            startCamera(context, imageUri);
                        } else {
                            startGallery(context);
                        }
                    }
                }).show();
    }

    public static void startCamera(Context context, Uri imageUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public static void startGallery(Context context) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }
}
