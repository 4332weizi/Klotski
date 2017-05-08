package io.auxo.klotski.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;

import io.auxo.klotski.R;
import io.auxo.klotski.model.Block;
import io.auxo.klotski.util.Dimension;
import io.auxo.klotski.util.L;

public class Klotski extends SurfaceView implements SurfaceHolder.Callback {

    private final String TAG = getClass().getSimpleName();

    private SurfaceHolder mSurfaceHolder;
    private DrawThread mDrawThread;

    private int[][] map = new int[5][4];

    private List<Block> mBlocks;

    private Rect mRect;
    private int mCellWidth;
    private int mCellHeight;

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

        mCellWidth = (mWidth - getPaddingLeft() - getPaddingRight()) / 4;
        mCellHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / 5;

        mRect = new Rect(0, 0, mWidth - getPaddingLeft() - getPaddingRight(), mHeight - getPaddingTop() - getPaddingBottom());

        updateBlocks();
    }

    private int touchedId = -1;
    private float mDownX = 0;
    private float mDownY = 0;
    private float mLastX = 0;
    private float mLastY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                L.i(this, "ACTION_DOWN");
                mDownX = event.getX();
                mDownY = event.getY();
                touchedId = getTouchedBlock((int) mDownX, (int) mDownY);
                break;
            case MotionEvent.ACTION_UP:
                L.i(this, "ACTION_UP");
                if (touchedId == -1) {
                    break;
                }
                Block b = mBlocks.get(touchedId);
                int newTop = Math.round((float) b.getRect().top / mCellHeight) * mCellHeight;
                int newLeft = Math.round((float) b.getRect().left / mCellWidth) * mCellWidth;
                b.getRect().offsetTo(newLeft, newTop);
                mDownX = 0;
                mDownY = 0;
                touchedId = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                L.i(this, "ACTION_MOVE");
                if (touchedId == -1) {
                    break;
                }
                Block block = mBlocks.get(touchedId);
                Rect rect = new Rect(block.getRect().left,
                        block.getRect().top,
                        block.getRect().right,
                        block.getRect().bottom);
                int newX = (int) (rect.left + event.getX() - mLastX);
                rect.offsetTo(newX, rect.top);
                if (canMove(rect, touchedId)) {
                    block.setRect(rect);
                    mBlocks.set(touchedId, block);
                }
                rect = new Rect(block.getRect().left,
                        block.getRect().top,
                        block.getRect().right,
                        block.getRect().bottom);
                int newY = (int) (rect.top + event.getY() - mLastY);
                rect.offsetTo(rect.left, newY);
                if (canMove(rect, touchedId)) {
                    block.setRect(rect);
                    mBlocks.set(touchedId, block);
                }
                break;
            default:
                break;
        }
        mLastX = event.getX();
        mLastY = event.getY();
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawThread = new DrawThread(mSurfaceHolder);
        mDrawThread.startDrawing();
        mDrawThread.setBlocks(mBlocks);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        L.i(this, "surfaceChanged");
        updateBlocks();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawThread.stopDrawing();
    }

    protected int getTouchedBlock(int x, int y) {
        for (int i = 0; i < mBlocks.size(); i++) {
            if (mBlocks.get(i).getRect().contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    protected boolean canMove(Rect rect, int ignore) {
        for (int i = 0; i < mBlocks.size(); i++) {
            if ((i != ignore && rect.intersect(mBlocks.get(i).getRect())) || !mRect.contains(rect)) {
                return false;
            }
        }
        return true;
    }

    public void setBlocks(List<Block> blocks) {
        mBlocks = blocks;
        if (mBlocks != null) {
            for (Block block : mBlocks) {
                if (block.getDrawable() == null) {
                    block.setDrawable(resolveDrawable(block.getType()));
                }
            }
        }
        updateBlocks();
    }

    protected void updateBlocks() {
        if (mBlocks != null) {
            for (int i = 0; i < mBlocks.size(); i++) {
                Block block = mBlocks.get(i);
                block.getRect().left = block.getX() * mCellWidth;
                block.getRect().top = block.getY() * mCellWidth;
                block.getRect().right = (block.getX() + block.getType().width()) * mCellWidth;
                block.getRect().bottom = (block.getY() + block.getType().height()) * mCellHeight;
                mBlocks.set(i, block);
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
            int spacing = (int) (mBlockSpacing / 2);
            if (canvas == null)
                return;
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(paint);
            for (Block block : mBlocks) {
                Drawable drawable = block.getDrawable();
                drawable.setBounds(block.getRect().left + spacing,
                        block.getRect().top + spacing,
                        block.getRect().right - spacing,
                        block.getRect().bottom - spacing);
                drawable.draw(canvas);
            }
        }
    }
}
