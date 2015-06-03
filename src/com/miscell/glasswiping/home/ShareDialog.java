package com.miscell.glasswiping.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.utils.Utils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by chenjishi on 15/5/12.
 */
public class ShareDialog extends Dialog implements View.OnClickListener {
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100;

    private static final String WX_APP_ID = "wxb1fb2707a1d61fe6";

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
                break;
            case R.id.share_timeline:
                checkStatus(SendMessageToWX.Req.WXSceneTimeline);
                shareWeChat(SendMessageToWX.Req.WXSceneTimeline);
                break;
        }
        dismiss();
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
}
