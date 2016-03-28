package org.team1540.vision;

import java.util.Arrays;

public class Shape {
    private boolean[] shape;
    private int markedCount;
    private int width;
    
    public Shape(int _width, int _height, boolean[] preallocated) {
        if (preallocated == null || preallocated.length != _width * _height) {
            preallocated = new boolean[_width*_height];
        }
        
        shape = preallocated;
        Arrays.fill(preallocated, false);
        width = _width;
    }
    
    public boolean set(int x, int y, boolean value) {
        if (shape[x+y*width] == !value) markedCount += value ? 1 : -1;
        shape[x+y*width] = value;
        return value;
    }
    
    public boolean mark(int x, int y) {
        if (!shape[x+y*width]) ++markedCount;
        shape[x+y*width] = true;
        return true;
    }
    
    public boolean mark(int index) {
        if (!shape[index]) ++markedCount;
        shape[index] = true;
        return true;
    }
    
    public boolean get(int x, int y) {
        return shape[x+y*width];
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
        return shape.length/width;
    }
    
    // make sure you don't edit the returned array
    public boolean[] getShape() {
        return shape;
    }
}
