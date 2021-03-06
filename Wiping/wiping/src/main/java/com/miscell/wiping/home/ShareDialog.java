package com.miscell.wiping.home;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import com.flurry.android.FlurryAgent;
import com.miscell.wiping.R;
import com.miscell.wiping.utils.Utils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileNotFoundException;

import static android.text.TextUtils.isEmpty;

/**
 * Created by chenjishi on 15/5/12.
 */
public class ShareDialog extends Dialog implements View.OnClickListener {
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100;

    private static final String WX_APP_ID = "wxb1fb2707a1d61fe6";
    private static final String WB_APP_ID = "4100060268";

    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    private IWXAPI mWXAPI;
    private ShareOption mShareOption;

    public ShareDialog(Context context, ShareOption option) {
        super(context, R.style.FullHeightDialog);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.share_dialog_layout);

        mShareOption = option;

        mWXAPI = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        mWXAPI.registerApp(WX_APP_ID);

        findViewById(R.id.share_session).setOnClickListener(this);
        findViewById(R.id.share_timeline).setOnClickListener(this);
        findViewById(R.id.share_weibo).setOnClickListener(this);
        findViewById(R.id.save_local).setOnClickListener(this);
    }

    @Override
    public void show() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(lp);

        super.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_session:
                checkStatus(SendMessageToWX.Req.WXSceneSession);
                shareWeChat(SendMessageToWX.Req.WXSceneSession);
                FlurryAgent.logEvent("wechat_session_click");
                break;
            case R.id.share_timeline:
                checkStatus(SendMessageToWX.Req.WXSceneTimeline);
                shareWeChat(SendMessageToWX.Req.WXSceneTimeline);
                FlurryAgent.logEvent("wechat_timeline_click");
                break;
            case R.id.share_weibo:
                shareToWB();
                FlurryAgent.logEvent("weibo_click");
                break;
            case R.id.save_local:
                saveImage();
                FlurryAgent.logEvent("image_save_click");
                break;
        }
        dismiss();
    }

    private void saveImage() {
        Context context = getContext();

        if (isEmpty(mShareOption.imagePath)) {
            Utils.showToast(context, R.string.photo_save_fail);
            return;
        }

        String fileName = System.currentTimeMillis() + ".png";
        ContentResolver contentResolver = context.getContentResolver();
        try {
            String imagePath = MediaStore.Images.Media.insertImage(contentResolver, mShareOption.imagePath, fileName, "Image from Wiping");
            if (!isEmpty(imagePath)) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                String filePath = getFilePathByContentResolver(Uri.parse(imagePath));
                Uri uri = Uri.fromFile(new File(filePath));
                intent.setData(uri);
                context.sendBroadcast(intent);

                Utils.showToast(context, R.string.photo_save_success);
            }
        } catch (FileNotFoundException e) {
            Utils.showToast(context, R.string.photo_save_fail);
        }
    }

    private String getFilePathByContentResolver(Uri uri) {
        if (null == uri) return null;

        Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
        String filePath = null;
        if (null == c) {
            throw new IllegalArgumentException(
                    "Query on " + uri + " returns null result.");
        }
        try {
            if ((c.getCount() != 1) || !c.moveToFirst()) {
            } else {
                filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
        } finally {
            c.close();
        }
        return filePath;
    }

    private void shareWeChat(final int type) {
        if (TextUtils.isEmpty(mShareOption.imagePath)) return;

        Bitmap bitmap = BitmapFactory.decodeFile(mShareOption.imagePath);
        sendImage(type, bitmap);
    }

    private void sendImage(int type, Bitmap bitmap) {
        Bitmap thumb = getThumbBitmap(bitmap);

        WXImageObject imageObject = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage(imageObject);
        msg.title = mShareOption.title;
        msg.description = mShareOption.description;
        msg.thumbData = Utils.bmpToByteArray(thumb, true);

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = "image" + System.currentTimeMillis();
        request.message = msg;
        request.scene = type;

        mWXAPI.sendReq(request);
    }

    private void checkStatus(int type) {
        Context context = getContext();

        if (!mWXAPI.isWXAppInstalled()) {
            Utils.showToast(context, R.string.wechat_not_install);
            return;
        }

        if (!mWXAPI.isWXAppSupportAPI()) {
            Utils.showToast(context, R.string.wechat_not_support);
            return;
        }

        if (type == SendMessageToWX.Req.WXSceneTimeline && mWXAPI.getWXAppSupportAPI() < TIMELINE_SUPPORTED_VERSION) {
            Utils.showToast(context, R.string.wechat_timeline_not_support);
        }
    }

    private Bitmap getThumbBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int dstWidth;
        int dstHeight;

        //thumbData do not exceed 32KB
        if (w < h) {
            dstHeight = THUMB_SIZE;
            dstWidth = (w * THUMB_SIZE) / h;
        } else {
            dstWidth = THUMB_SIZE;
            dstHeight = (h * THUMB_SIZE) / w;
        }

        return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
    }

    private void shareToWB() {

    }
}
