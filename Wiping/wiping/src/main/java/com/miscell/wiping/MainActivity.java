package com.miscell.wiping;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.miscell.wiping.home.*;
import com.miscell.wiping.utils.DirectoryUtils;
import com.miscell.wiping.utils.Feed;
import com.miscell.wiping.utils.NetworkRequest;
import com.miscell.wiping.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends BaseActivity implements Listener<String>, ErrorListener,
        SwipeRefreshLayout.OnRefreshListener, OnPageEndListener, LoaderManager.LoaderCallbacks<Cursor> {
    protected Uri mImageUri;
    private static final int LOADER_ID = 10012;

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

    public void onMoreButtonClicked(View view) {
        startActivity(new Intent(this, AboutActivity.class));
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {MediaStore.Images.Media.DATA};
        return new CursorLoader(this, mImageUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null != data) {
            int imageIndex = data.getColumnIndex(MediaStore.Images.Media.DATA);
            data.moveToFirst();
            String filePath = data.getString(imageIndex);
            handleImage(filePath);
        }
        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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

    private void handleImage(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;

        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra(PhotoActivity.IMAGE_PATH, filePath);
        startActivity(intent);
    }
}
