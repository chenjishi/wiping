package com.miscell.wiping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.miscell.wiping.raindrops.GlassWipeActivity;
import com.miscell.wiping.utils.Blur;
import com.miscell.wiping.utils.Feed;
import com.miscell.wiping.utils.ImageInfo;
import com.miscell.wiping.utils.NetworkRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjishi on 15/3/14.
 */
public class DetailsActivity extends BaseActivity implements Response.Listener<String>,
        Response.ErrorListener {
    private ImageListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_details);

        Feed feed = getIntent().getParcelableExtra("feed");
        String title = feed.title;
        if (!TextUtils.isEmpty(title)) {
            if (title.length() > 10) {
                title = title.substring(0, 10);
            }
            setTitle(title);
        }

        mListAdapter = new ImageListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.image_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addItemDecoration(new SpaceDecoration(this));

        showLoadingView();
        NetworkRequest.get(feed.url, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        hideLoadingView();
    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            Document document = Jsoup.parse(response);
            if (null == document) return;

            Element content = document.getElementById("picture");
            if (null == content) return;

            ArrayList<ImageInfo> imageList = new ArrayList<>();
            Elements images = content.select("img");
            for (Element img : images) {
                String url = img.attr("src");
                if (!TextUtils.isEmpty(url)) {
                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.imageUrl = url;
                    imageList.add(imageInfo);
                }
            }

            if (imageList.size() > 0) {
                mListAdapter.addData(imageList);
            }
        }
        hideLoadingView();
    }

    private static class ImageListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<ImageInfo> mImageList = new ArrayList<>();
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

        public void addData(List<ImageInfo> dataList) {
            mImageList.addAll(dataList);
            mBitmaps = new SparseArray<>(mImageList.size());
            notifyDataSetChanged();
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.list_image_item, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, final int position) {

            final ImageInfo imageInfo = mImageList.get(position);

            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.imageView.getLayoutParams();
            Bitmap bitmap = mBitmaps.get(position);
            if (null != bitmap) {
                lp.width = mImageWidth;
                lp.height = mImageWidth * bitmap.getHeight() / bitmap.getWidth();
                holder.imageView.setLayoutParams(lp);
                holder.imageView.setImageBitmap(bitmap);
                holder.coverView.setLayoutParams(lp);
            } else {
                mImageLoader.get(imageInfo.imageUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        Bitmap bmp = response.getBitmap();
                        if (null != bmp) {
                            lp.width = mImageWidth;
                            lp.height = mImageWidth * bmp.getHeight() / bmp.getWidth();
                            imageInfo.width = lp.width;
                            imageInfo.height = lp.height;

                            Bitmap blurBitmap = Blur.fastblur(mContext, bmp, 20);
                            holder.imageView.setLayoutParams(lp);
                            holder.imageView.setImageBitmap(blurBitmap);
                            mBitmaps.put(position, blurBitmap);
                            holder.coverView.setLayoutParams(lp);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }, 400, 0);
            }

            holder.itemLayout.setTag(imageInfo);
            holder.itemLayout.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mImageList.size();
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == v.getTag()) return;

                ImageInfo imageInfo = (ImageInfo) v.getTag();
                Intent intent = new Intent(mContext, GlassWipeActivity.class);
                intent.putExtra("imgsrc", imageInfo.imageUrl);
                intent.putExtra("width", imageInfo.width);
                intent.putExtra("height", imageInfo.height);
                mContext.startActivity(intent);
            }
        };
    }

    private static final class ItemViewHolder extends RecyclerView.ViewHolder {

        public FrameLayout itemLayout;

        public ImageView imageView;

        public View coverView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemLayout = (FrameLayout) itemView.findViewById(R.id.item_layout);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            coverView = itemView.findViewById(R.id.cover_view);
        }
    }

    private static class SpaceDecoration extends RecyclerView.ItemDecoration {

        private float density;

        public SpaceDecoration(Context context) {
            density = context.getResources().getDisplayMetrics().density;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (position == RecyclerView.NO_POSITION) return;

            int margin = (int) (density * 8);

            outRect.set(margin, margin, margin, 0);
        }
    }
}
