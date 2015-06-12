package com.miscell.glasswiping.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import com.miscell.glasswiping.BaseActivity;
import com.miscell.glasswiping.DetailsActivity;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.utils.Feed;
import com.miscell.glasswiping.utils.NetworkRequest;
import com.miscell.glasswiping.volley.Response;
import com.miscell.glasswiping.volley.VolleyError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, Response.Listener<String>,
        Response.ErrorListener, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FeedListAdapter mListAdapter;

    private int mPage = 1;
    private boolean mIsLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        ((ViewGroup) gridView.getParent()).addView(emptyView);
        gridView.setEmptyView(emptyView);

        mListAdapter = new FeedListAdapter(this);
        gridView.setAdapter(mListAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(this);

        showLoadingView();
        request();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mSwipeRefreshLayout.setRefreshing(false);
        mIsLoading = false;
        hideLoadingView();
    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            if (mPage == 1) mListAdapter.clearData();

            ArrayList<Feed> feedList = parseFeedList(response);
            if (null != feedList && feedList.size() > 0) {
                mListAdapter.addData(feedList);
            }
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mIsLoading = false;
        hideLoadingView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Feed feed = mListAdapter.getItem(position);
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("feed", feed);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mPage = 1;
        request();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mLastItemIndex == mListAdapter.getCount() - 2) {
            if (!mIsLoading) {
                mPage++;
                request();
            }
        }
    }

    private int mLastItemIndex;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
    }

    private void request() {
        String url = String.format("http://www.meizitu.com/a/list_1_%d.html", mPage);
        NetworkRequest.get(url, this, this);
        mIsLoading = true;
    }

    private ArrayList<Feed> parseFeedList(String html) {
        Document document = Jsoup.parse(html);
        if (null == document) return null;

        Elements pics = document.getElementsByClass("pic");
        if (null == pics || pics.size() == 0) return null;

        ArrayList<Feed> feedList = new ArrayList<Feed>();
        for (Element pic : pics) {
            Element link = pic.child(0);
            if (null != link) {
                Feed feed = new Feed();
                feed.url = link.attr("href");

                Element img = link.child(0);
                if (null != img) {
                    feed.imageUrl = img.attr("src");
                    String title = img.attr("alt");
                    title = title.replaceAll("<b>|</b>", "");
                    feed.title = title;

                }
                feedList.add(feed);
            }
        }

        return feedList;
    }
}
