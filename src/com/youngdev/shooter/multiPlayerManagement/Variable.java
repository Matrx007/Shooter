package com.youngdev.shooter.multiPlayerManagement;

public class Variable {
    private String name;
    private Pointer pointer;

    public Variable(String name, Pointer pointer) {
        this.name = name;
        this.pointer = pointer;
    }

    public String getName() {
        return name;
    }

    public Pointer getPointer() {
        return pointer;
    }
}
