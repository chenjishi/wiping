package com.miscell.wiping;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import com.miscell.wiping.home.LoadingView;
import com.miscell.wiping.utils.Utils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by chenjishi on 15/3/11.
 */
public class BaseActivity extends FragmentActivity {
    protected boolean mHideTitle;
    protected int mTitleResId;
    protected float mDensity;
    protected LayoutInflater mInflater;
    private LoadingView mLoadingView;
    protected FrameLayout mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDensity = getResources().getDisplayMetrics().density;
        mInflater = LayoutInflater.from(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        mRootView = (FrameLayout) findViewById(android.R.id.content);

        if (!mHideTitle) {
            int resId = mTitleResId == 0 ? R.layout.base_title_layout : mTitleResId;
            mInflater.inflate(resId, mRootView);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.BOTTOM);
        lp.topMargin = mHideTitle ? 0 : dp2px(48);
        mRootView.addView(mInflater.inflate(layoutResID, null), lp);
    }

    protected void setContentView(int layoutResID, int titleResId) {
        mTitleResId = titleResId;
        setContentView(layoutResID);
    }

    protected void setContentView(int layoutResID, boolean hideTitle) {
        mHideTitle = hideTitle;
        setContentView(layoutResID);
    }

    public void onBackClicked(View v) {
        finish();
    }

    protected void setRightButtonIcon(int resId) {
        final ImageButton button = (ImageButton) findViewById(R.id.btn_right);
        button.setImageResource(resId);
        button.setVisibility(View.VISIBLE);
    }

    public void onRightButtonClicked(View v) {

    }

    protected void showLoadingView() {
        showLoadingView(0, 0xFFF0F0F0);
    }

    protected void showLoadingView(int margin, int color) {
        if (null == mLoadingView) {
            mLoadingView = new LoadingView(this);
            mLoadingView.setBackgroundColor(color);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                MATCH_PARENT, MATCH_PARENT);
        lp.topMargin = dp2px(48 + margin);
        lp.gravity = Gravity.BOTTOM;
        mLoadingView.setLayoutParams(lp);

        ViewParent viewParent = mLoadingView.getParent();
        if (null != viewParent) {
            ((ViewGroup) viewParent).removeView(mLoadingView);
        }

        mRootView.addView(mLoadingView);
    }

    protected void hideLoadingView() {
        if (null != mLoadingView) {
            ViewParent viewParent = mLoadingView.getParent();
            if (null != viewParent) {
                ((ViewGroup) viewParent).removeView(mLoadingView);
            }
            mLoadingView = null;
        }
    }

    protected void setError(String tips) {
        if (null != mLoadingView) {
            mLoadingView.setError(tips);
        }
    }

    protected void setError() {
        if (null != mLoadingView) {
            int resId = R.string.network_invalid;
            if (Utils.isNetworkConnected(this)) {
                resId = R.string.server_error;
            }

            mLoadingView.setError(getString(resId));
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        final TextView titleText = (TextView) findViewById(R.id.tv_title);
        titleText.setText(title);
        titleText.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    protected int dp2px(int d) {
        return (int) (d * mDensity + .5f);
    }
}
