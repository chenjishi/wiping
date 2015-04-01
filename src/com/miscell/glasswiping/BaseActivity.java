package com.miscell.glasswiping;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by chenjishi on 15/3/11.
 */
public class BaseActivity extends FragmentActivity {
    protected boolean mHideTitle;
    protected int mTitleResId;
    protected float mDensity;
    protected LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDensity = getResources().getDisplayMetrics().density;
        mInflater = LayoutInflater.from(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        FrameLayout contentView = (FrameLayout) findViewById(android.R.id.content);
        contentView.setBackgroundColor(0xFFF0F0F0);

        if (!mHideTitle) {
            int resId = mTitleResId == 0 ? R.layout.base_title_layout : mTitleResId;
            mInflater.inflate(resId, contentView);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.BOTTOM);
        lp.topMargin = mHideTitle ? 0 : dp2px(48);
        contentView.addView(mInflater.inflate(layoutResID, null), lp);
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
