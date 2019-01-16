package com.youngdev.shooter.modules;

import com.sun.istack.internal.NotNull;
import com.youngdev.shooter.Main;

public abstract class Module {

    protected Main main;
    public int dataIndex;

    public Module(@NotNull Main main, int dataIndex) {
        this.main = main;
        this.dataIndex = dataIndex;
    }

    public abstract void init();
    public abstract void read(byte[] bytes);
    public abstract void tick();
    public abstract void draw();
}
