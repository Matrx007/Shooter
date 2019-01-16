package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.youngdev.shooter.multiPlayerManagement.WorldObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Plant extends WorldObject {
    ArrayList<Piece> leaf;
    private boolean prevCollision;
    private Random random;
    private int type;
    public final int Type = 6;
    public boolean collision;

    private static final int TYPE_SINGLE =0, TYPE_PATCH=1;
    
    public Plant(int x, int y, Color color) {
        super(6, 2, 6);
        this.x = x;
        this.y = y;
        this.prevCollision = false;
        
        leaf = new ArrayList<>();

        collision = false;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;

        type = random.nextInt(10)==1 ? TYPE_PATCH : TYPE_SINGLE;

        if(type == TYPE_SINGLE) {
            Player.Vector4 bounds = spawn(0, 0, 10, 4, 8, color);
            smallestX = bounds.x1;
            smallestY = bounds.y1;
            largestX = bounds.x2;
            largestY = bounds.y2;
//            System.out.println("Plant");
//            System.out.println("smallestX = " + smallestX);
//            System.out.println("smallestY = " + smallestY);
//            System.out.println("largestX = " + largestX);
//            System.out.println("largestY = " + largestY);
        } else if(type == TYPE_PATCH) {
            int numSubPatches = random.nextInt(4)+4;
            for(int i = 0; i < numSubPatches; i++) {
                double distance = random.nextDouble()*32+32;
                double angle = random.nextDouble()*360;
                int xx = (int)(this.x+Math.cos(Math.toRadians(angle))*distance);
                int yy = (int)(this.y+Math.sin(Math.toRadians(angle))*distance);
                Player.Vector4 bounds = spawn(xx, yy, 70, 20, 32, color);
                smallestX = Math.min(smallestX, bounds.x1);
                smallestY = Math.min(smallestY, bounds.y1);
                largestX = Math.max(largestX, bounds.x2);
                largestY = Math.max(largestY, bounds.y2);
            }
//            System.out.println("Patch");
//            System.out.println("smallestX = " + smallestX);
//            System.out.println("smallestY = " + smallestY);
//            System.out.println("largestX = " + largestX);
//            System.out.println("largestY = " + largestY);
        }

        mask = new Mask.Rectangle(smallestX, smallestY, largestX-smallestX, largestY-smallestY);
    }

    @Override
    public void update(Input input) {
        prevCollision = collision;
        collision = false;
        ArrayList<GameObject> entities = new ArrayList<>();

        Iterator<GameObject> it;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            GameObject obj = it.next();
            if (obj instanceof Arrow || (obj instanceof Healable && obj.depth > this.depth))
                if(obj.mask != null)
                    if (obj.mask.isColliding(this.mask)) {
                        entities.add(obj);
                        collision = true;
                   }
        }

        if(collision != prevCollision) needsUpdate = true;

        boolean collision_EffectivelyFinal = collision;

        leaf.forEach(leave -> {
            boolean touched = false;
            if(collision_EffectivelyFinal) {/* && !prevCollision) {*/
                for(GameObject obj : entities) {
                    if (Fly.distance(leave.x, leave.y, obj.x, obj.y) < 16) {
                        touched = true;
                        break;
                    }
                }
            }
            if(touched)
                leave.speed = 24d;
            leave.step+=Main.toSlowMotion(random.nextInt((int) Math.max(1, leave.speed * 10))/10d);
            leave.speed=Math.max(0, leave.speed-0.125);
            leave.addX = (int)(Math.cos(Math.toRadians(leave.step))*2d);
            leave.addY = (int)(Math.sin(Math.toRadians(leave.step))*2d);
        });
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

    private Player.Vector4 spawn(int offX, int offY, int numLeaf, int numBloom, int distanceLimit, Color color) {
        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;

        for (int i = 0; i < numLeaf; i++) {
            double distance = random.nextDouble()*distanceLimit;
            double angle = random.nextDouble()*360;
            int xx = (int)(offX+x+Math.cos(Math.toRadians(angle))*distance);
            int yy = (int)(offY+y+Math.sin(Math.toRadians(angle))*distance);
            int size = random.nextInt(2) + 5;
            int tone = random.nextInt(10);
            Color clr = new Color(49 + tone, 107 + tone, 38 + tone);
            leaf.add(new Piece(xx, yy, size, clr, random.nextInt(359)));

            smallestX = Math.min(smallestX, xx - size / 2);
            smallestY = Math.min(smallestY, yy - size / 2);
            largestX = Math.max(largestX, (int) Math.ceil(xx + size / 2d));
            largestY = Math.max(largestY, (int) Math.ceil(yy + size / 2d));
        }

        for (int i = 0; i < numBloom; i++) {
            double distance = random.nextDouble()*distanceLimit;
            double angle = random.nextDouble()*360;
            int xx = (int)(offX+x+Math.cos(Math.toRadians(angle))*distance);
            int yy = (int)(offY+y+Math.sin(Math.toRadians(angle))*distance);
            int size = random.nextInt(2) + 4;
            int tone = random.nextInt(10);

            Color clr = new Color(color.getRed() + tone, color.getGreen() + tone, color.getBlue() + tone);
            leaf.add(new Piece(xx, yy, size, clr, random.nextInt(359)));

            smallestX = Math.min(smallestX, xx - size / 2);
            smallestY = Math.min(smallestY, yy - size / 2);
            largestX = Math.max(largestX, (int) Math.ceil(xx + size / 2d));
            largestY = Math.max(largestY, (int) Math.ceil(yy + size / 2d));
        }

        return new Player.Vector4(smallestX, smallestY, largestX, largestY);
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
