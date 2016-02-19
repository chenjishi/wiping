package com.miscell.wiping.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.miscell.wiping.DetailsActivity;
import com.miscell.wiping.R;
import com.miscell.wiping.utils.Feed;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjishi on 16/2/18.
 */
public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_FEED = 0;

    private static final int ITEM_TYPE_FOOTER = 1;

    private final List<Feed> mDataList = new ArrayList<>();

    private LayoutInflater mInflater;

    private int mImageWidth;

    private Context mContext;

    public FeedListAdapter(Context context) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float density = metrics.density;
        mContext = context;

        float padding = density * 8;
        mImageWidth = (metrics.widthPixels - (int) (3 * padding)) / 2;

        mInflater = LayoutInflater.from(mContext);
    }

    public void addData(List<Feed> dataList) {
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void clear() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_FOOTER) {
            View view = mInflater.inflate(R.layout.load_more, parent, false);
            return new FootViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.feed_list_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == mDataList.size() ? ITEM_TYPE_FOOTER : ITEM_TYPE_FEED;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_TYPE_FEED) {
            final Feed feed = mDataList.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemHolder.imageView.getLayoutParams();
            lp.width = mImageWidth;
            lp.height = mImageWidth;

            itemHolder.imageView.setImageURI(Uri.parse(feed.imageUrl));
            itemHolder.imageView.setLayoutParams(lp);
            itemHolder.titleText.setText(feed.title);

            itemHolder.itemLayout.setTag(position);
            itemHolder.itemLayout.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size() > 0 ? mDataList.size() + 1 : 0;
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v.getTag()) return;

            int index = (Integer) v.getTag();
            Feed feed = mDataList.get(index);
            Intent intent = new Intent(mContext, DetailsActivity.class);
            intent.putExtra("feed", feed);
            mContext.startActivity(intent);
        }
    };

    private static final class ItemViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout itemLayout;

        public SimpleDraweeView imageView;

        public TextView titleText;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.image_view);
            titleText = (TextView) itemView.findViewById(R.id.label_title);
        }
    }
}
