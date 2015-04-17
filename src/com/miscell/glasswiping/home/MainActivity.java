package com.miscell.glasswiping.home;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import com.miscell.glasswiping.AboutActivity;
import com.miscell.glasswiping.BaseActivity;
import com.miscell.glasswiping.DetailsActivity;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.utils.*;
import com.miscell.glasswiping.volley.Response;
import com.miscell.glasswiping.volley.VolleyError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, Response.Listener<String>,
        Response.ErrorListener, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    protected Uri mImageUri;
    private static final int LOADER_ID = 10012;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FeedListAdapter mListAdapter;

    private int mPage = 1;
    private boolean mIsLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRightButtonIcon(R.drawable.ic_menu_more);
        findViewById(R.id.ic_arrow).setVisibility(View.GONE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        ((ViewGroup) gridView.getParent()).addView(emptyView);
        gridView.setEmptyView(emptyView);

        findViewById(R.id.photo_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoChooser();
            }
        });

        mListAdapter = new FeedListAdapter(this);
        gridView.setAdapter(mListAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(this);

        //clear temp files, such as shared image or temp upgrade apk
        File tempFile = new File(DirectoryUtils.getTempCacheDir());
        if (tempFile.exists()) {
            FileUtils.delete(tempFile);
        }

        request();

    }

    @Override
    public void onBackClicked(View v) {
    }

    @Override
    public void onRightButtonClicked(View v) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mSwipeRefreshLayout.setRefreshing(false);
        mIsLoading = false;
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

    private void showPhotoChooser() {
        File file = new File(DirectoryUtils.getTempCacheDir(), "Pic.jpg");
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
        }

        mImageUri = Uri.fromFile(file);
        Utils.choosePhoto(this, mImageUri);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) return;

        Log.i("test", "# onActivityResult " + requestCode);

        if (requestCode == Utils.REQUEST_CODE_CAMERA) {
            String filePath = mImageUri.getPath();
            handleImage(filePath);
        } else if (requestCode == Utils.REQUEST_CODE_GALLERY) {
            mImageUri = data.getData();
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private void handleImage(String filePath) {
        Log.i("test", "#file path " + filePath);

    }
}
