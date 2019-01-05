package com.youngdev.shooter.multiPlayerManagement;

public class Pointer {
    private int loc, size;
    public static final int SIZE_INT = 4;
    public static final int SIZE_BOOLEAN = 1;

    public Pointer(int loc, int size) {
        this.loc = loc;
        this.size = size;
    }

    public int getLoc() {
        return loc;
    }

    public int getSize() {
        return size;
    }
}
