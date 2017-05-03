package io.auxo.klotski.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import io.auxo.klotski.R;

public class Klotski extends SurfaceView implements SurfaceHolder.Callback {

    private final String TAG = getClass().getSimpleName();

    private SurfaceHolder mSurfaceHolder;
    private DrawThread mDrawThread;

    private int mChessManWidth;
    private int mChessManHeight;

    public Klotski(Context context) {
        this(context, null);
    }

    public Klotski(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Klotski(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        setWillNotDraw(false);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.Klotski);
        int color = ta.getColor(R.styleable.Klotski_color, Color.TRANSPARENT);
        ta.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // 初始化游戏棋盘大小
        int mHeight = getHeight();
        int mWidth = getWidth();

        Log.i(TAG, "高度调整前 height -> " + mHeight + " width -> " + mWidth);

        ViewGroup.LayoutParams params = getLayoutParams();

        if (mWidth / 4 <= mHeight / 5) {
            // 需减小高度
            params.height = 5 * mWidth / 4;
            params.width = mWidth;
        } else {
            // 需减小宽度
            params.height = mHeight;
            params.width = 4 * mHeight / 5;
        }

        setLayoutParams(params);

        Log.i(TAG, "高度调整后 height -> " + params.height + " width -> " + params.width);

        mChessManWidth = (mWidth - getPaddingLeft() - getPaddingRight()) / 4;
        mChessManHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / 5;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawThread = new DrawThread(mSurfaceHolder);
        mDrawThread.startDrawing();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawThread.stopDrawing();
    }

    private class DrawThread extends Thread {

        private SurfaceHolder mSurfaceHolder;
        private boolean running;

        public DrawThread(SurfaceHolder mSurfaceHolder) {
            this.mSurfaceHolder = mSurfaceHolder;
        }

        public void startDrawing() {
            running = true;
            start();
        }

        public void stopDrawing() {
            running = false;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        public void draw(Canvas canvas) {
            if (canvas == null)
                return;
            Paint paint = new Paint();
            paint.setColor(Color.rgb(0, 0, 0));
            canvas.drawRect(0, 0, mChessManWidth, mChessManHeight, paint);
        }
    }
}
