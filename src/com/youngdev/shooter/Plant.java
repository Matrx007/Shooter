package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Plant extends GameObject {
    ArrayList<Piece> leaf;
    private boolean prevCollision;
    private Random random;
    
    public Plant(int x, int y, Color color) {
        super(6, 4);
        this.x = x;
        this.y = y;
        this.prevCollision = false;
        
        leaf = new ArrayList<>();

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;
        
        int numLeaf = 10;
        for(int i = 0; i < numLeaf; i++) {
            int xx = x + random.nextInt(12)-6;
            int yy = y + random.nextInt(12)-6;
            int size = random.nextInt(2)+5;
            int tone = random.nextInt(10);
            Color clr = new Color(49+tone, 107+tone, 38+tone);
            leaf.add(new Piece(xx, yy, size, clr, random.nextInt(359)));

            smallestX = Math.min(smallestX, xx-size/2);
            smallestY = Math.min(smallestY, yy-size/2);
            largestX = Math.max(largestX, (int)Math.ceil(xx+size/2d));
            largestY = Math.max(largestY, (int)Math.ceil(yy+size/2d));
        }
        
        int numBloom = 4;
        for(int i = 0; i < numBloom; i++) {
            int xx = x + random.nextInt(16)-8;
            int yy = y + random.nextInt(16)-8;
            int size = random.nextInt(2)+4;
            int tone = random.nextInt(10);
            Color clr = new Color(color.getRed()+tone, color.getGreen()+tone, color.getBlue()+tone);
            leaf.add(new Piece(xx, yy, size, clr, random.nextInt(359)));

            smallestX = Math.min(smallestX, xx-size/2);
            smallestY = Math.min(smallestY, yy-size/2);
            largestX = Math.max(largestX, (int)Math.ceil(xx+size/2d));
            largestY = Math.max(largestY, (int)Math.ceil(yy+size/2d));
        }

        mask = new Mask.Rectangle(smallestX, smallestY, largestX-smallestX, largestY-smallestY);
    }

    @Override
    public void update(Input input) {
        boolean collision = false;

        Iterator<GameObject> it;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            GameObject obj = it.next();
            if (obj instanceof Arrow || (obj instanceof Healable && obj.depth > this.depth))
                if(obj.mask != null)
                    if (obj.mask.isColliding(this.mask) ||
                            Fly.distance(x, y, obj.x, obj.y) < 16) {
                        collision = true;
                        break;
                   }
        }

        boolean collision_EffectivelyFinal = collision;

        leaf.forEach(leave -> {
            if(collision_EffectivelyFinal) {/* && !prevCollision) {*/
                leave.speed = 24d;
            }
            leave.step+=Main.toSlowMotion(random.nextInt((int) Math.max(1, leave.speed * 10))/10d);
            leave.speed=Math.max(0, leave.speed-0.125);
            leave.addX = (int)(Math.cos(Math.toRadians(leave.step))*2d);
            leave.addY = (int)(Math.sin(Math.toRadians(leave.step))*2d);
        });

        prevCollision = collision;
    }

    @Override
    public void render(Renderer r) {
        leaf.forEach(leave -> r.fillRectangle(leave.x-leave.size/2+leave.addX, leave.y-leave.size/2+leave.addY,
                leave.size, leave.size, leave.color));
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    public class Piece {
        public int x, y, addX, addY, size;
        public double speed, step;
        public Color color;

        public Piece(int x, int y, int size, Color clr, int step) {
            this.x = x;
            this.y = y;
            this.speed = 0;
            this.color = clr;
            this.step = step;
            this.addX = 0;
            this.addY = 0;
            this.size = size;

        }
    }
}
