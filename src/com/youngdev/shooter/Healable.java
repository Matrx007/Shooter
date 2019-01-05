package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.youngdev.shooter.multiPlayerManagement.WorldObject;

import java.util.Random;

public abstract class Healable extends WorldObject {
    public int health;
    public Random random;
    public boolean isEnemy, hasCollision;

    public Healable(int type, int x, int y, int w, int h, int health, int index, int depth, boolean isEnemy, boolean hasCollision) {
        super(index, depth, type);
        this.x = x;
        this.y = y;
        this.isEnemy = isEnemy;
        this.mask = new Mask.Rectangle(x-w/2d, y-h/2d, w, h);
        this.health = health;
        this.hasCollision = hasCollision;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;
    }

    public abstract void update(Input i);
    public abstract void render(Renderer r);

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
