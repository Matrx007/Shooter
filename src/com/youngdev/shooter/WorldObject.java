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
}
