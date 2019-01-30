package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Terrain extends WorldObject {
    private ArrayList<Patch> pieces;

    public static final int TYPE_SMALL_ROCKS = 1,
                            TYPE_DIRT_PATCH = 2;
    public final int Type = 11;
    public int type;

    public Terrain(int x, int y, int type) {
        super(9, 0, 11);
        this.x = x;
        this.y = y;
        this.type = type;

        // HERE: Fix depth
        Random random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        pieces = new ArrayList<>();
        int range = 48;
        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;
        range = random.nextInt(32)+48;
        int num = random.nextInt(64)+16;
        for(int i = 0; i < num; i++) {
            int angle = random.nextInt(359);
            int distance = (int)(Math.abs(random.nextGaussian())*range);
            int xx = x+(int)(Math.cos(Math.toRadians(angle))*distance);
            int yy = y+(int)(Math.sin(Math.toRadians(angle))*distance);
//                    int xx = x-random.nextInt(range*2)+range;
//                    int yy = y-random.nextInt(range*2)+range;
            int size;
            if(random.nextBoolean()) {
                size = random.nextInt(6) + 5;
                int tone = random.nextInt(5) + 3;
                Color baseColor = new Color(60, 120, 90);
                Color color = new Color(baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                pieces.add(new Patch(xx, yy, size, color));
            } else {
                size = random.nextInt(4)+2;
                int tone = random.nextInt(20);
                Color baseColor = new Color(80, 80, 80);
                Color color = new Color(baseColor.getRed()+tone,
                        baseColor.getGreen()+tone,
                        baseColor.getBlue()+tone);
                pieces.add(new Patch(xx, yy, size, color));
                pieces.add(new Patch(xx, yy, size, color));
            }
            smallestX = Math.min(smallestX, xx - size / 2);
            smallestY = Math.min(smallestY, yy - size / 2);
            largestX = Math.max(largestX, (int) Math.ceil(xx + size / 2d));
            largestY = Math.max(largestY, (int) Math.ceil(yy + size / 2d));
        }

        mask = new Mask.Rectangle(smallestX, smallestY, largestX-smallestX, largestY-smallestY);
    }

    @Override
    public void update(Input input) {

    }

    @Override
    public void render(Renderer r) {
        pieces.forEach(obj -> obj.render(r));
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    public class Patch {
        public int x, y, size;
        public Color color;

        public Patch(int x, int y, int size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }

        public void render(Renderer r) {
            r.fillRectangle(x-size/2, y-size/2, size, size, color);
        }
    }
}
