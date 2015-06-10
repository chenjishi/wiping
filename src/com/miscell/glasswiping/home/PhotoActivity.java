package com.miscell.glasswiping.home;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.flurry.android.FlurryAgent;
import com.miscell.glasswiping.BaseActivity;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.raindrops.RainDropsView;
import com.miscell.glasswiping.utils.Blur;
import com.miscell.glasswiping.utils.DirectoryUtils;
import com.miscell.glasswiping.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by chenjishi on 15/4/1.
 */
public class PhotoActivity extends BaseActivity implements View.OnClickListener {
    private static final int MAX_IMAGE_WIDTH = 1080;
    private static final String BLURRED_IMG_NAME = "blurred_image.png";

    private static final int SCALED_WIDTH = 400;

    public static final String IMAGE_PATH = "image_path";
    private ImageView mImageView;
    private FrameLayout mContentView;
    private ImageButton mShareButton;

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

        mContentView = (FrameLayout) findViewById(android.R.id.content);
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
                RainDropsView rainView = new RainDropsView(PhotoActivity.this);
                mContentView.addView(rainView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                rainView.setBlurredImagePath(getFilesDir() + BLURRED_IMG_NAME);

                mImageView.setVisibility(View.VISIBLE);

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp2px(48), dp2px(48));
                lp.gravity = Gravity.RIGHT;
                mShareButton = new ImageButton(PhotoActivity.this);
                mShareButton.setBackgroundResource(R.drawable.hightlight_bkg);
                mShareButton.setImageResource(R.drawable.ic_social_share);
                mShareButton.setOnClickListener(PhotoActivity.this);
                mContentView.addView(mShareButton, lp);
            }
        });
    }

    private ProgressDialog mProgress;

    private void generateImage() {
        mContentView.destroyDrawingCache();
        mContentView.setDrawingCacheEnabled(true);

        Bitmap bitmap = mContentView.getDrawingCache();

        if (null != bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int requiredWidth = MAX_IMAGE_WIDTH;
            int requiredHeight = (int) (MAX_IMAGE_WIDTH * height * 1.f / width);

            final Rect srcRect = new Rect(0, 0, width, height);
            final Rect dstRect = new Rect(0, 0, requiredWidth, requiredHeight);

            Bitmap newBitmap = Bitmap.createBitmap(requiredWidth, requiredHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, srcRect, dstRect, null);

            final String filePath = DirectoryUtils.getTempCacheDir() + "share.png";
            File file = new File(filePath);
            storeImage(newBitmap, file);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    share(filePath);
                }
            });
        }

        mContentView.setDrawingCacheEnabled(false);
    }

    private void share(String filePath) {
        if (null != mProgress && mProgress.isShowing()) mProgress.dismiss();

        if (TextUtils.isEmpty(filePath)) return;

        ShareOption shareOption = new ShareOption();
        shareOption.title = getString(R.string.app_name);
        shareOption.description = getString(R.string.share_description);
        shareOption.imagePath = filePath;
        shareOption.url = "http://www.u148.net/";

        ShareDialog shareDialog = new ShareDialog(this, shareOption);
        shareDialog.show();
        mShareButton.setVisibility(View.VISIBLE);
    }

    private void storeImage(Bitmap image, File pictureFile) {
        if (pictureFile == null) return;

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    @Override
    public void onClick(View v) {
        if (null == mProgress) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage(getString(R.string.generating_image));
        }
        mProgress.show();
        mShareButton.setVisibility(View.GONE);

        new Thread() {
            @Override
            public void run() {
                generateImage();
            }
        }.start();

        FlurryAgent.logEvent("share_button_click");
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
