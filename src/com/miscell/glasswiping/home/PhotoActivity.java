package com.miscell.glasswiping.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.miscell.glasswiping.BaseActivity;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.raindrops.RainDropsView;
import com.miscell.glasswiping.utils.Blur;
import com.miscell.glasswiping.utils.Utils;

import java.io.File;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by chenjishi on 15/4/1.
 */
public class PhotoActivity extends BaseActivity {
    private static final String BLURRED_IMG_NAME = "blurred_image.png";

    private static final int SCALED_WIDTH = 400;

    public static final String IMAGE_PATH = "image_path";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_photo, true);

        Bundle extras = getIntent().getExtras();
        if (null == extras) {
            finish();
            return;
        }

        String filePath = extras.getString(IMAGE_PATH);

        mImageView = (ImageView) findViewById(R.id.image_view);
        setPreview(filePath);
    }

    private int setPreview(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        int width = getResources().getDisplayMetrics().widthPixels;
        int w = options.outWidth;
        int h = options.outHeight;
        int height = (int) (width * h * 1.0f / w);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;


        final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        if (null != bitmap) {
            mImageView.setImageBitmap(bitmap);

            new Thread() {
                @Override
                public void run() {
                    blur(bitmap);
                }
            }.start();
        }

        return height;
    }

    private void blur(Bitmap bitmap) {
        int width = SCALED_WIDTH;
        int height = (int) (width * bitmap.getHeight() * 1.f / bitmap.getWidth());

        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        Bitmap bmp = Bitmap.createBitmap(scaleBitmap.getWidth(), scaleBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(scaleBitmap, 0, 0, null);
        scaleBitmap.recycle();

        File file = new File(getFilesDir() + BLURRED_IMG_NAME);
        Bitmap newImg = Blur.fastblur(this, bmp, 12);
        Utils.storeImage(newImg, file);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout container = (FrameLayout) findViewById(R.id.container_view);
                RainDropsView rainView = new RainDropsView(PhotoActivity.this);
                container.addView(rainView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                rainView.setBlurredImagePath(getFilesDir() + BLURRED_IMG_NAME);

                mImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
