package org.team1540.vision;

public class Shape {
    private final boolean[] shape;
    private int markedCount;
    private final int width, height;

    public Shape(int width, int height) {
        shape = new boolean[width * height];
        this.width = width;
        this.height = height;
    }

    public boolean set(int x, int y, boolean value) {
        if (shape[x + y * width] == !value)
            markedCount += value ? 1 : -1;
        shape[x + y * width] = value;
        return value;
    }

    public boolean mark(int x, int y) {
        if (!shape[x + y * width])
            ++markedCount;
        shape[x + y * width] = true;
        return true;
    }

    public boolean mark(int index) {
        if (!shape[index])
            ++markedCount;
        shape[index] = true;
        return true;
    }

    public boolean get(int x, int y) {
        return shape[x + y * width];
    }

    public boolean get(int index) {
        return shape[index];
    }

    public int getCount() {
        return markedCount;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return shape.length / width;
    }

    // make sure you don't edit the returned array
    public boolean[] getShape() {
        return shape;
    }
}
