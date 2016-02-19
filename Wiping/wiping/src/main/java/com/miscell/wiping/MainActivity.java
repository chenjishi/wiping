package com.miscell.wiping;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.miscell.wiping.home.FeedListAdapter;
import com.miscell.wiping.home.MarginDecoration;
import com.miscell.wiping.home.OnListScrollListener;
import com.miscell.wiping.home.OnPageEndListener;
import com.miscell.wiping.utils.Feed;
import com.miscell.wiping.utils.NetworkRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements Listener<String>, ErrorListener,
        SwipeRefreshLayout.OnRefreshListener, OnPageEndListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FeedListAdapter mListAdapter;

    private int mPage = 1;

    private OnListScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main, R.layout.home_title_layout);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mScrollListener = new OnListScrollListener(layoutManager);
        mScrollListener.setOnPageEndListener(this);
        mListAdapter = new FeedListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.image_grid_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addItemDecoration(new MarginDecoration(this));
        recyclerView.addOnScrollListener(mScrollListener);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == mListAdapter.getItemCount() - 1 ? 2 : 1;
            }
        });

        showLoadingView();
        request();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mSwipeRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
        hideLoadingView();
    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            if (mPage == 1) mListAdapter.clear();

            ArrayList<Feed> feedList = parseFeedList(response);
            if (null != feedList && feedList.size() > 0) {
                mListAdapter.addData(feedList);
            }
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
        hideLoadingView();
    }

    @Override
    public void onPageEnd() {
        mPage += 1;
        request();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mPage = 1;
        request();
    }

    private void request() {
        mScrollListener.setIsLoading(true);

        String url = String.format("http://www.meizitu.com/a/list_1_%d.html", mPage);
        NetworkRequest.get(url, this, this);
    }

    private ArrayList<Feed> parseFeedList(String html) {
        Document document = Jsoup.parse(html);
        if (null == document) return null;

        Elements pics = document.getElementsByClass("pic");
        if (null == pics || pics.size() == 0) return null;

        ArrayList<Feed> feedList = new ArrayList<>();
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
