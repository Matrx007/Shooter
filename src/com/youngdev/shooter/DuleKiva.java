package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

public class DuleKiva extends WorldObject {

    private int step;
    private double angle;

    public DuleKiva(int x, int y) {
        super(12, 19, 14);
        this.x = x;
        this.y = y;
        this.mask = new Mask.Rectangle(x-12, -12, 24,24);
    }

    @Override
    public void update(Input input) {
        step+=8;

        angle = Math.pow(Math.cos(Math.toRadians(step)), 10)*
                Math.signum(Math.cos(Math.toRadians(step)))*360d;

    }

    @Override
    public void render(Renderer r) {
        double[][] points = new double[][]{
                Fly.rotatePoint(x-12, y-12, x, y, -angle-90),
                Fly.rotatePoint(x+12, y-12, x, y, -angle-90),
                Fly.rotatePoint(x+12, y+12, x, y, -angle-90),
                Fly.rotatePoint(x-12, y+12, x, y, -angle-90)
        };
        Fly.fillPoly(points, new Color(16, 16, 48), r);
        points = new double[][]{
                Fly.rotatePoint(x-8, y-8, x, y, angle-90),
                Fly.rotatePoint(x+8, y-8, x, y, angle-90),
                Fly.rotatePoint(x+8, y+8, x, y, angle-90),
                Fly.rotatePoint(x-8, y+8, x, y, angle-90)
        };
        Fly.fillPoly(points, new Color(48, 48, 96), r);
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
