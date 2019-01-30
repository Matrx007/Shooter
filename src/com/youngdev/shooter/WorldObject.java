package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.util.Random;

public abstract class WorldObject extends GameObject {
    public int type;
    protected Random random;

    public WorldObject(int index, int depth, int type) {
        random = new Random();
        this.depth = depth;
        this.index = index;
        this.type = type * 1024 + random.nextInt(1024);
    }

    void checkLocation(double prevX, double prevY) {
        int x = (int)this.x / Main.chunkSize;
        int y = (int)this.y / Main.chunkSize;
        int prvX = (int)prevX / Main.chunkSize;
        int prvY = (int)prevY / Main.chunkSize;
        if(x != prvX || y != prvY) {
            Main.main.move(this, prvX, prvY, x, y);
        }
    }
}
