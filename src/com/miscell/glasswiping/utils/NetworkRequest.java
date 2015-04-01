package com.miscell.glasswiping.utils;

import android.app.ActivityManager;
import android.content.Context;
import com.miscell.glasswiping.volley.RequestQueue;
import com.miscell.glasswiping.volley.Response;
import com.miscell.glasswiping.volley.toolbox.ImageLoader;
import com.miscell.glasswiping.volley.toolbox.StringRequest;
import com.miscell.glasswiping.volley.toolbox.Volley;

/**
 * Created by chenjishi on 15/3/11.
 */
public class NetworkRequest {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache((cacheSize)));
    }

    public static RequestQueue getRequestQueue() {
        if (null != mRequestQueue) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static ImageLoader getImageLoader() {
        if (null != mImageLoader) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static void get(String url,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        queue.add(new StringRequest(url, listener, errorListener));
    }
}
