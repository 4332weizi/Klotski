package io.auxo.klotski.model;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;

public class Block {

    private int id;
    private Type type;
    private int x;
    private int y;
    private Rect rect;
    private Drawable drawable;

    public Block(Type type, int x, int y, Drawable drawable) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.drawable = drawable;
        rect = new Rect(x, y, x + type.width(), y + type.height());
    }

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public enum Type {

        RECT_1x1(1, 1, 1),
        RECT_1x2(2, 1, 2),
        RECT_2x1(3, 2, 1),
        RECT_2x2(4, 2, 2);

        int value;
        int width;
        int height;

        Type(int value, int width, int height) {
            this.value = value;
            this.width = width;
            this.height = height;
        }

        public int value() {
            return value;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public static Type parse(@IntRange(from = 1, to = 4) int type) {
            return values()[type - 1];
        }
    }

}
