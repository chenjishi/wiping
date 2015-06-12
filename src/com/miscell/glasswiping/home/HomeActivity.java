package com.miscell.glasswiping.home;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.miscell.glasswiping.AboutActivity;
import com.miscell.glasswiping.BaseActivity;
import com.miscell.glasswiping.Config;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.raindrops.RainDropsView;
import com.miscell.glasswiping.utils.DirectoryUtils;
import com.miscell.glasswiping.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenjishi on 15/6/10.
 */
public class HomeActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    protected Uri mImageUri;
    private static final int LOADER_ID = 10012;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home, R.layout.home_title_layout);

        RainDropsView rainDropsView = (RainDropsView) findViewById(R.id.rain_drop_view);
        rainDropsView.setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textView = (TextView) findViewById(R.id.btn_browse);
        final boolean browseMode = Config.getBrowseMode(this);
        if (browseMode) {
            textView.setOnClickListener(mImageClickListener);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) return;

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    public void onCameraClicked(View view) {
        File file = new File(DirectoryUtils.getTempCacheDir(), "Pic.jpg");
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
        }

        mImageUri = Uri.fromFile(file);
        PushUpDialog dialog = new PushUpDialog(this, mImageUri);
        dialog.show();
    }

    public void onMoreButtonClicked(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {MediaStore.Images.Media.DATA};
        return new CursorLoader(this, mImageUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (null != cursor) {
            int imageIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(imageIndex);
            handleImage(filePath);
        }
        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void handleImage(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;

        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra(PhotoActivity.IMAGE_PATH, filePath);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) return;

        if (requestCode == Utils.REQUEST_CODE_CAMERA) {
            String filePath = mImageUri.getPath();
            handleImage(filePath);
        } else if (requestCode == Utils.REQUEST_CODE_GALLERY) {
            mImageUri = data.getData();
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private final View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };
}
