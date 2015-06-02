package com.miscell.glasswiping.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import jy.DangMaLa.R;
import static jy.DangMaLa.utils.Constants.REQUEST_CODE_CAMERA;
import static jy.DangMaLa.utils.Constants.REQUEST_CODE_GALLERY;

/**
 * Created by chenjishi on 15/4/22.
 */
public class PushUpDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    protected Uri mImageUri;

    public PushUpDialog(Context context, Uri uri) {
        super(context, R.style.PopupDialogStyle);
        mContext = context;
        mImageUri = uri;

        setCanceledOnTouchOutside(true);
        setContentView(R.layout.popup_dialog_layout);

        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_gallery).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                startCamera();
                break;
            case R.id.btn_gallery:
                startGallery();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
        dismiss();
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void startGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE_GALLERY);
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
}
