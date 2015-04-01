package com.miscell.glasswiping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.miscell.glasswiping.raindrops.GlassWipeActivity;
import com.miscell.glasswiping.utils.Blur;
import com.miscell.glasswiping.utils.Feed;
import com.miscell.glasswiping.utils.NetworkRequest;
import com.miscell.glasswiping.volley.Response;
import com.miscell.glasswiping.volley.VolleyError;
import com.miscell.glasswiping.volley.toolbox.ImageLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by chenjishi on 15/3/14.
 */
public class DetailsActivity extends BaseActivity implements Response.Listener<String>,
        Response.ErrorListener, AdapterView.OnItemClickListener {
    private ImageListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_details);

        Feed feed = getIntent().getParcelableExtra("feed");
        setTitle(feed.title);

        mListAdapter = new ImageListAdapter(this);

        ListView listView = (ListView) findViewById(R.id.image_list_view);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(this);

        NetworkRequest.get(feed.url, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            Document document = Jsoup.parse(response);
            if (null == document) return;

            Element content = document.getElementById("picture");
            if (null == content) return;

            ArrayList<String> imageList = new ArrayList<String>();
            Elements images = content.select("img");
            for (Element img : images) {
                String url = img.attr("src");
                if (!TextUtils.isEmpty(url)) {
                    imageList.add(url);
                }
            }

            if (imageList.size() > 0) {
                mListAdapter.addData(imageList);
            }
        }
    }

    private static class ImageListAdapter extends BaseAdapter {
        private ArrayList<String> mImageList = new ArrayList<String>();
        private LayoutInflater mInflater;
        private ImageLoader mImageLoader;

        private SparseArray<Bitmap> mBitmaps;
        private int mImageWidth;

        private Context mContext;

        public ImageListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mImageLoader = NetworkRequest.getImageLoader();

            mContext = context;

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float density = metrics.density;
            mImageWidth = metrics.widthPixels - 2 * ((int) (density * 8 + .5f));
        }

        public void addData(ArrayList<String> dataList) {
            mImageList.addAll(dataList);
            mBitmaps = new SparseArray<Bitmap>(mImageList.size());
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public String getItem(int position) {
            return mImageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView;
            if (null == convertView) {
                itemView = mInflater.inflate(R.layout.list_image_item, parent, false);
            } else {
                itemView = convertView;
            }

            final ImageView imageView = (ImageView) itemView.findViewById(R.id.image_view);

            final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            Bitmap bitmap = mBitmaps.get(position);
            if (null != bitmap) {
                lp.width = mImageWidth;
                lp.height = mImageWidth * bitmap.getHeight() / bitmap.getWidth();
                imageView.setLayoutParams(lp);
                imageView.setImageBitmap(bitmap);
            } else {
                mImageLoader.get(mImageList.get(position), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        Bitmap bmp = response.getBitmap();
                        if (null != bmp) {
                            lp.width = mImageWidth;
                            lp.height = mImageWidth * bmp.getHeight() / bmp.getWidth();
                            long t = System.currentTimeMillis();
                            Bitmap blurBitmap = Blur.fastblur(mContext, bmp, 20);
                            Log.i("test", "time " + (System.currentTimeMillis() - t) + " width " + blurBitmap.getWidth());
                            imageView.setLayoutParams(lp);
                            imageView.setImageBitmap(blurBitmap);
                            mBitmaps.put(position, blurBitmap);
                        }

                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }, 400, 0);
            }

            return itemView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, GlassWipeActivity.class);
        intent.putExtra("imgsrc", mListAdapter.getItem(position));
        startActivity(intent);
    }
}
