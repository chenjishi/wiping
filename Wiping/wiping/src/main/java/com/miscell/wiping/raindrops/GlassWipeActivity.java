package com.miscell.wiping.raindrops;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.miscell.wiping.R;
import com.miscell.wiping.utils.Blur;
import com.miscell.wiping.utils.NetworkRequest;
import com.miscell.wiping.utils.Utils;

import java.io.File;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by chenjishi on 15/3/17.
 */
public class GlassWipeActivity extends Activity implements ImageLoader.ImageListener {
    private static final String BLURRED_IMG_NAME = "blurred_image.png";
    private static final int SCALED_WIDTH = 400;

    private ImageView mImageView;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int sdk_int = Build.VERSION.SDK_INT;
        if (sdk_int < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();

            if (sdk_int >= 19) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }

        Bundle bundle = getIntent().getExtras();
        int width = bundle.getInt("width");
        int height = bundle.getInt("height");
        Log.i("test", "width " + width + " height " + height);

        if (width <= height) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.activity_wipe);



        mImageView = (ImageView) findViewById(R.id.image_view);

        String imageUrl = bundle.getString("imgsrc");

        findViewById(R.id.tip_text).setVisibility(View.VISIBLE);

        mImageView = (ImageView) findViewById(R.id.image_view);
        NetworkRequest.getImageLoader().get(imageUrl, this);

        mPlayer = MediaPlayer.create(this, R.raw.shower);
        mPlayer.setLooping(true);
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        final Bitmap bitmap = response.getBitmap();
        if (null != bitmap) {
            mImageView.setImageBitmap(bitmap);

            new Thread() {
                @Override
                public void run() {
                    blur(bitmap);
                }
            }.start();
        }
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
                RainDropsView rainView = new RainDropsView(GlassWipeActivity.this);
                container.addView(rainView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                rainView.setBlurredImagePath(getFilesDir() + BLURRED_IMG_NAME);

                mImageView.setVisibility(View.VISIBLE);
                mPlayer.start();
            }
        });
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
