package com.miscell.glasswiping.home;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.utils.Feed;
import com.miscell.glasswiping.utils.NetworkRequest;
import com.miscell.glasswiping.volley.toolbox.ImageLoader;
import com.miscell.glasswiping.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjishi on 15/2/28.
 */
public class FeedListAdapter extends BaseAdapter {
    private final ImageLoader mImageLoader;
    private LayoutInflater mInflater;
    private int mImageWidth;

    private ArrayList<Feed> mFeedList = new ArrayList<Feed>();

    public FeedListAdapter(Context context) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float density = metrics.density;

        float padding = density * 8;
        mImageWidth = (metrics.widthPixels - (int) (3 * padding)) / 2;

        mImageLoader = NetworkRequest.getImageLoader();
        mInflater = LayoutInflater.from(context);
    }

    public void addData(List<Feed> dataList) {
        mFeedList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void clearData() {
        mFeedList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFeedList.size();
    }

    @Override
    public Feed getItem(int position) {
        return mFeedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.feed_list_item, parent, false);
            holder = new ViewHolder();

            holder.titleLabel = (TextView) convertView.findViewById(R.id.label_title);
            holder.imageView = (NetworkImageView) convertView.findViewById(R.id.image_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Feed feed = getItem(position);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.imageView.getLayoutParams();
        lp.width = mImageWidth;
        lp.height = mImageWidth;

        holder.imageView.setImageUrl(feed.imageUrl, mImageLoader);
        holder.imageView.setLayoutParams(lp);
        holder.titleLabel.setText(feed.title);

        return convertView;
    }

    private static class ViewHolder {

        public NetworkImageView imageView;

        public TextView titleLabel;
    }
}
