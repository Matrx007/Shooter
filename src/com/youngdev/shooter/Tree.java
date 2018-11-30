package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Tree extends GameObject {

    private ArrayList<Leaf> leaf;
    private Random random;
    private boolean prevCollision, fliesInside;

    public Tree(int x, int y) {
        super(11, 20);
        this.x = x;
        this.y = y;
        this.fliesInside = true;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        leaf = new ArrayList<>();


        // HERE: Tree gen V 1.0
        Rectangle bounds = spawnLeaf(24, 36, 24, 0, 0);

        mask = new Mask.Rectangle((double)bounds.x, (double)bounds.y, bounds.width, bounds.height);
    }

    @Override
    public void update(Input input) {
        boolean collision = false;

        Iterator<GameObject> it;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            GameObject obj = it.next();
            if (obj instanceof Arrow || (obj instanceof Healable && obj.depth > this.depth))
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
            if(collision_EffectivelyFinal) {/* && !this.prevCollision) {*/
                leave.speed = 24d;
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
            leave.speed=Math.max(1, leave.speed-0.25);
            leave.addX = (int)(Math.cos(Math.toRadians(leave.step))*2d);
            leave.addY = (int)(Math.sin(Math.toRadians(leave.step))*2d);
        });

        this.prevCollision = collision_EffectivelyFinal;
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
            int xx = (int)x - random.nextInt(distanceLimit*2) + distanceLimit + offX;
            int yy = (int)y - random.nextInt(distanceLimit*2) + distanceLimit + offY;
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
