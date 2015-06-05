package com.miscell.glasswiping.raindrops;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import com.miscell.glasswiping.R;
import com.miscell.glasswiping.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by chenjishi on 15/3/17.
 */
public class RainDropsView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final float DEFAULT_STROKE_WIDTH = 30.0f;

    private static final float TOUCH_TOLERANCE = 4;

    private static final int MAXIMUM_DROPS = 60;
    private static final int TYPE_FALLING = 0;
    private static final int TYPE_SCALE = 1;
    private Bitmap mDropsBitmap, mBlurBitmap, mCoverBitmap;

    private final Rect mSrcRect = new Rect();
    private final RectF mDstRect = new RectF();

    private float mX, mY, mDensity;

    private int mWidth, mHeight;

    private final Path mPath = new Path();

    private final Random mRandom = new Random();

    private Canvas mCanvas;

    private ValueAnimator mAnimator;

    private static final float GRAVITY_EARTH = SensorManager.GRAVITY_EARTH;

    private ArrayList<Drop> mDropList = new ArrayList<Drop>();
    private ArrayList<Drop> mRecycleList = new ArrayList<Drop>();

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RainDropsView(Context context) {
        this(context, null);
    }

    public RainDropsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RainDropsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.density;
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        mPaint.setAlpha(0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
        mPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH * mDensity);

        mBlurBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBlurBitmap);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        mDropsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.raindrops, options);
        mCoverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rain_tile2, options);

        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setDuration(2000);
        mAnimator.addUpdateListener(this);
    }

    public void setBlurredImagePath(String filePath) {
        Bitmap blurredBmp = BitmapFactory.decodeFile(filePath);
        if (null == blurredBmp) return;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAlpha(98);

        Bitmap bitmap = Utils.scaleCenterCrop(blurredBmp, mWidth, mHeight);
        mDstRect.set(0, 0, mWidth, mHeight);
        mCanvas.drawBitmap(bitmap, null, mDstRect, null);
        mCanvas.drawBitmap(mCoverBitmap, new Rect(0, 0, mCoverBitmap.getWidth(),
                mCoverBitmap.getHeight()), mDstRect, paint);

        bitmap.recycle();
        blurredBmp.recycle();

        if (!mAnimator.isRunning()) mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mAnimator && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    private int randInt(int min, int max) {
        return mRandom.nextInt((max - min) + 1) + min;
    }

    private void initDrop(Drop drop) {
        drop.width = 32;
        drop.height = 32;
        drop.type = randInt(0, 1);
        drop.startTime = System.currentTimeMillis();
        drop.offset = randInt(0, 7);
        drop.x = randInt(0, mWidth - 32);
        drop.y = randInt(0, mHeight - 32);
        drop.speed = 300 + (float) Math.random() * 400;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (mDropList.size() < MAXIMUM_DROPS) {
            Drop drop = new Drop();
            initDrop(drop);
            mDropList.add(drop);
        } else {
            for (int i = 0; i < mRecycleList.size(); i++) {
                Drop drop = mRecycleList.get(i);
                initDrop(drop);
                mDropList.add(mRecycleList.remove(i));
            }
        }

        for (Drop drop : mDropList) {
            if (drop.type == TYPE_FALLING) {
                float secs = ((System.currentTimeMillis() - drop.startTime)) * 1.f / 1000;
                float h = GRAVITY_EARTH * secs * secs * 0.5f;
                drop.y += (int) h;
            } else {
                drop.width -= 0.1f;
                drop.height -= 0.1f;
            }
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;
        if (null == mBlurBitmap) return;

        canvas.drawBitmap(mBlurBitmap, 0, 0, null);
        mCanvas.drawPath(mPath, mPaint);

        int x, y, offset;
        for (int i = 0; i < mDropList.size(); i++) {
            Drop drop = mDropList.get(i);
            offset = drop.offset * 32;
            mSrcRect.set(offset, 0, offset + 32, 32);
            x = drop.x;
            y = drop.y;
            mDstRect.set(x, y, x + drop.width, y + drop.height);

            canvas.drawBitmap(mDropsBitmap, mSrcRect, mDstRect, null);
            if (drop.y < 0 || drop.y > h) {
                mRecycleList.add(mDropList.remove(i));
            }

            if (drop.width < 0 || drop.height < 0) {
                mRecycleList.add(mDropList.remove(i));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.reset();
                mPath.moveTo(x, y);
                mX = x;
                mY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
        }

        return true;
    }

    private int measureWidth(int measureSpec) {
        int width;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (MeasureSpec.EXACTLY == specMode) {
            width = specSize;
        } else {
            width = getPaddingLeft() + getPaddingRight();
            if (MeasureSpec.AT_MOST == specMode) {
                width = Math.min(width, specSize);
            }
        }

        return width;
    }

    private int measureHeight(int measureSpec) {
        int height;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (MeasureSpec.EXACTLY == specMode) {
            height = specSize;
        } else {
            height = getPaddingTop() + getPaddingBottom();
            if (MeasureSpec.AT_MOST == specMode) {
                height = Math.min(height, specSize);
            }
        }

        return height;
    }
}
