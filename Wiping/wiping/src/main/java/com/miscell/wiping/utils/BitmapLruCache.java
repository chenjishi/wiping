package com.miscell.wiping.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import com.android.volley.toolbox.ImageLoader;

/**
 * Created by chenjishi on 14-6-20.
 */
@SuppressLint("NewApi")
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            result = value.getByteCount();
        } else {
            result = value.getRowBytes() * value.getHeight();
        }
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + value);
        }
        return result;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}