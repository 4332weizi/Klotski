package io.auxo.klotski.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;

import io.auxo.klotski.R;
import io.auxo.klotski.model.Block;
import io.auxo.klotski.util.Dimension;

public class Klotski extends SurfaceView implements SurfaceHolder.Callback {

    private final String TAG = getClass().getSimpleName();

    private SurfaceHolder mSurfaceHolder;
    private DrawThread mDrawThread;

    private int[][] map = new int[5][4];

    private List<Block> mBlocks;

    private int mChessManWidth;
    private int mChessManHeight;

    private float mBlockSpacing;
    private Drawable mDrawable1x1;
    private Drawable mDrawable1x2;
    private Drawable mDrawable2x1;
    private Drawable mDrawable2x2;

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
        mBlockSpacing = ta.getDimension(R.styleable.Klotski_blockSpacing, Dimension.dp2px(getContext(), 3));
        mDrawable1x1 = ta.getDrawable(R.styleable.Klotski_blockDrawable1x1);
        mDrawable1x2 = ta.getDrawable(R.styleable.Klotski_blockDrawable1x2);
        mDrawable2x1 = ta.getDrawable(R.styleable.Klotski_blockDrawable2x1);
        mDrawable2x2 = ta.getDrawable(R.styleable.Klotski_blockDrawable2x2);
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
        mDrawThread.setBlocks(mBlocks);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawThread.stopDrawing();
    }

    public void setBlocks(List<Block> blocks) {
        this.mBlocks = blocks;
        if (mBlocks != null) {
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).getDrawable() == null) {
                    blocks.get(i).setDrawable(resolveDrawable(blocks.get(i).getType()));
                }
            }
        }
        if (mDrawThread != null) {
            mDrawThread.setBlocks(mBlocks);
        }
    }

    protected Drawable resolveDrawable(Block.Type type) {
        if (type == Block.Type.RECT_1x1) {
            return mDrawable1x1;
        } else if (type == Block.Type.RECT_1x2) {
            return mDrawable1x2;
        } else if (type == Block.Type.RECT_2x1) {
            return mDrawable2x1;
        } else if (type == Block.Type.RECT_2x2) {
            return mDrawable2x2;
        }
        return null;
    }

    private class DrawThread extends Thread {

        private SurfaceHolder mSurfaceHolder;
        private boolean running;

        private List<Block> mBlocks;

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

        public void setBlocks(List<Block> blocks) {
            this.mBlocks = blocks;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                if (mBlocks == null) {
                    continue;
                }
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
            for (Block block : mBlocks) {
                Drawable drawable = block.getDrawable();
                int spacing = (int) (mBlockSpacing / 2);
                Rect rect = new Rect(block.getX() * mChessManWidth + spacing,
                        block.getY() * mChessManHeight + spacing,
                        (block.getX() + block.getType().width()) * mChessManWidth - spacing,
                        (block.getY() + block.getType().height()) * mChessManHeight - spacing);
                drawable.setBounds(rect);
                drawable.draw(canvas);
            }
        }
    }
}
