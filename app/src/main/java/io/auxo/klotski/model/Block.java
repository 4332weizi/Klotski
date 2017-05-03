package io.auxo.klotski.model;

import android.support.annotation.IntRange;

public class Block {

    private Type type;
    private int x;
    private int y;

    public Block(Type type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
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
