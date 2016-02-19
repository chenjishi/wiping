package com.miscell.wiping;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import com.miscell.wiping.utils.FileUtils;
import com.miscell.wiping.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-17
 * Time: 下午11:20
 * To change this template use File | Settings | File Templates.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(R.string.about);
        mContext = this;

        findViewById(R.id.feedback).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);

        cacheThread.start();
        initView();
    }

    private Thread cacheThread = new Thread(new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            String size = FileUtils.getImageCacheSize(AboutActivity.this);
            Message msg = Message.obtain();
            msg.obj = size;
            msg.what = 1;
            handler.sendMessage(msg);
        }
    });

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1 && !isFinishing()) {
                String cache = String.format(getString(R.string.cache_clear), msg.obj);
                ((Button) findViewById(R.id.btn_clear)).setText(cache);
            }
        }
    };

    private void initView() {
        String versionName = Utils.getCurrentVersionName(this);
        if (null != versionName) {
            Button versionText = (Button) findViewById(R.id.version_text);
            versionText.setText(versionName);
        }
    }

    private int mClickCount;

    public void onVersionTextClicked(View view) {
        mClickCount += 1;
        if (mClickCount >= 8) {
            Config.saveBrowseMode(this, true);
            Utils.showToast(this, R.string.easter_egg_tip);
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chenjishi313@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));

        startActivity(Intent.createChooser(intent, getString(R.string.email_choose)));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.feedback:
                sendFeedback();
                break;
            case R.id.btn_clear:
                new ClearCacheTask().execute();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private class ClearCacheTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progress = new ProgressDialog(AboutActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setCancelable(false);
            progress.setMessage("正在清除...");
            progress.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            FileUtils.clearCache(AboutActivity.this);
            mContext.deleteDatabase("webview.db");
            mContext.deleteDatabase("webviewCache.db");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            Utils.showToast(AboutActivity.this, "清除缓存成功!");
            String cache = String.format(mContext.getString(R.string.cache_clear), "0KB");
            ((Button) findViewById(R.id.btn_clear)).setText(cache);
        }
    }
}
