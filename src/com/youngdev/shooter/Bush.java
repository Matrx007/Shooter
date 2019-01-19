package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Bush extends WorldObject {

    private ArrayList<Leaf> leaf;
    private Random random;
    private boolean prevCollision;
    public boolean fliesInside;
    public final int Type = 1;
    public boolean collision;

    public Bush(int x, int y) {
        super(11, 11, 1);
        this.x = x;
        this.y = y;
        this.fliesInside = true;
        this.solid = true;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        leaf = new ArrayList<>();

        collision = false;

        // HERE: Bush gen V 1.0
        Rectangle bounds = spawnLeaf(24, 36, 24,
                0, 0);
        if(random.nextInt(6)==1) {
            int numBerries = 5 + random.nextInt(5);
            for (int i = 0; i < numBerries; i++) {
                double distance = random.nextDouble() * 30;
                double angle = random.nextDouble() * 360;
                int xx = (int) (x + Math.cos(Math.toRadians(angle)) * distance);
                int yy = (int) (y + Math.sin(Math.toRadians(angle)) * distance);

                Leaf berry = new Leaf(xx, yy, 5, random.nextInt(10) - 5,
                        random.nextInt(359));
                berry.baseColor = new Color(100, 40, 40);
                leaf.add(berry);
            }
        }


        mask = new Mask.Rectangle((double)bounds.x+1, (double)bounds.y+1, bounds.width-2, bounds.height-2);
        this.aabbComponent = new AABBComponent(mask);
    }

    @Override
    public void update(Input input) {
        this.prevCollision = collision;
        collision = false;

        Iterator<GameObject> it;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            GameObject obj = it.next();
            if (obj instanceof Arrow || (obj instanceof Healable))
                if(obj.mask != null)
                    if (obj.mask.isColliding(mask) ||
                            Fly.distance(x, y, obj.x, obj.y) < 64) {
                        collision = true;
                        break;
                    }
        }

        boolean collision_EffectivelyFinal = collision;
        boolean spawn = random.nextInt(16) == 3;

        leaf.forEach(leave -> {
            if(collision_EffectivelyFinal && !prevCollision) {/* && !this.prevCollision) {*/
                leave.speed = 12d;
                if(fliesInside) {
                    if (spawn) {
                        for (int i = 0; i < random.nextInt(5) + 3; i++) {
                            int xx = (int) x + random.nextInt(24) - 12;
                            int yy = (int) y + random.nextInt(24) - 12;
                            Main.main.flies.add(new Fly(xx, yy, false));
                        }
                        fliesInside = false;
                    }
                }
            }
            leave.step+=Main.toSlowMotion(leave.speed);
            leave.speed=Math.max(1, leave.speed-0.125);
            leave.addX = (int)(Math.cos(Math.toRadians(leave.step))*2d);
            leave.addY = (int)(Math.sin(Math.toRadians(leave.step))*2d);
        });
    }

    @Override
    public void render(Renderer r) {
        leaf.forEach(leave -> r.fillRectangle(leave.x-leave.size/2+leave.addX, leave.y-leave.size/2+leave.addY,
                leave.size, leave.size, leave.getColor()));
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    private Rectangle spawnLeaf(int minLeaf, int maxLeaf, int distanceLimit, int offX, int offY) {
        int numLeaf = random.nextInt(maxLeaf-minLeaf) + minLeaf;
        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;
        for (int i = 0; i < numLeaf; i++) {
            // HERE: Create a leave
            double distance = random.nextDouble()*distanceLimit;
            double angle = random.nextDouble()*360;
            int xx = (int)(x+Math.cos(Math.toRadians(angle))*distance);
            int yy = (int)(y+Math.sin(Math.toRadians(angle))*distance);
            int size = (int)((random.nextInt(30)+24f)*(i/numLeaf+0.5f));
            leaf.add(new Leaf(xx, yy, size, random.nextInt(15), random.nextInt(359)));

            smallestX = Math.min(smallestX, xx-size/2);
            smallestY = Math.min(smallestY, yy-size/2);
            largestX = Math.max(largestX, (int)Math.ceil(xx+size/2d));
            largestY = Math.max(largestY, (int)Math.ceil(yy+size/2d));
        }

        return new Rectangle(smallestX, smallestY, largestX-smallestX, largestY-smallestY);
    }

    public class Leaf {
        public int x, y, addX, addY, size, tone;
        public double speed, step;
        public Color baseColor = new Color(49, 107, 38);

        public Leaf(int x, int y, int size, int tone, int step) {
            this.x = x;
            this.y = y;
            this.step = step;
            this.addX = 0;
            this.addY = 0;
            this.speed = 1d;
            this.size = size;
            this.tone = tone;
        }

        public Color getColor() {
            return new Color(
                    baseColor.getRed()+tone,
                    baseColor.getGreen()+tone,
                    baseColor.getBlue()+tone);
        }
    }
}
