package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.util.ArrayList;
import java.util.Iterator;

public class FlyGroup extends WorldObject {
    private ArrayList<Fly> flies;
    private int range;

    public FlyGroup(int x, int y, int numFlies) {
        super(16, 15, 16);
        flies = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.range = (int)(random.nextInt(
                Main.chunkSize/4)+Main.chunkSize/4f);

        for(int i = 0; i < numFlies; i++) {
            double angle = random.nextInt(359);
            double distance = range;
            int xxx = x + (int) (Math.cos(Math.toRadians(angle))*distance);
            int yyy = y + (int) (Math.sin(Math.toRadians(angle))*distance);
            flies.add(new Fly(xxx, yyy));
        }

        findBounds();
    }

    public FlyGroup(int x, int y, int numFlies, boolean flyAway) {
        super(16, 15, 16);
        flies = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.range = (int)(random.nextInt(
                Main.chunkSize/4)+Main.chunkSize/4f);

        double direction = 0;
        if(flyAway) {
            double minDis = Double.MAX_VALUE;
            Healable closestEnemy = null;
            Iterator<GameObject> it;
            for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
                GameObject obj = it.next();
                if (obj instanceof Healable && !((Healable) obj).isEnemy) {
                    double distance = Math.hypot((x - obj.x), (y - obj.y));
                    if (distance <= minDis) {
                        minDis = distance;
                        closestEnemy = (Healable) obj;
                    }
                }
            }
            if(closestEnemy != null)
                direction = Fly.angle(closestEnemy.x, closestEnemy.y,
                        x, y) - 180;
        }

        for(int i = 0; i < numFlies; i++) {
            double angle = random.nextInt(359);
            double distance = range;
            int xxx = x + (int) (Math.cos(Math.toRadians(angle))*distance);
            int yyy = y + (int) (Math.sin(Math.toRadians(angle))*distance);
            Fly fly = new Fly(xxx, yyy);
            flies.add(fly);
            if(flyAway) {
                fly.wandering = false;
                fly.direction = direction;
            }
        }
        findBounds();
        this.depth = 15*1024+random.nextInt(1024);
    }

    @Override
    public void update(Input i) {
        int smallestX=Integer.MAX_VALUE;
        int smallestY=Integer.MAX_VALUE;
        int largestX=Integer.MIN_VALUE;
        int largestY=Integer.MIN_VALUE;

        // ###### FIND BOUNDS #######
        for (int j = flies.size()-1; j>=0; j--) {
            Fly fly = flies.get(j);
            smallestX = (int)Math.min(smallestX, fly.x);
            smallestY = (int)Math.min(smallestY, fly.y);
            largestX = (int)Math.max(largestX, fly.x);
            largestY = (int)Math.max(largestY, fly.y);
        }

        findBounds();

        // ###### SEARCH FOR NEARBY ENTITIES #######

        boolean found = false;
        ArrayList<GameObject> nearEntities = new ArrayList<>();
        for (GameObject entity : Main.main.visibleChunkEntities) {
            if(entity.mask instanceof Mask.Rectangle &&
                    entity.mask.isColliding(mask)) {
                if(AdvancedMath.inRange(entity.x, entity.y,
                        smallestX-range, smallestY-range,
                        largestX-smallestX+range,
                        largestY-smallestY+range)) {
                    found = true;
                    break;
                }
            }
        }

        // ####### UPDATE FLIES ########

        boolean _found = found;
        flies.removeIf((fly) -> {
            if(fly.wandering)
                if(_found) {
                    fly.wandering = false;
                    fly.angle = random.nextInt(360);
                }
            fly.update(i);
            return fly.dead;
        });
    }

    private void findBounds() {
        int smallestX=Integer.MAX_VALUE;
        int smallestY=Integer.MAX_VALUE;
        int largestX=Integer.MIN_VALUE;
        int largestY=Integer.MIN_VALUE;

        // ###### FIND BOUNDS #######
        for (int j = flies.size()-1; j>=0; j--) {
            Fly fly = flies.get(j);
            smallestX = (int)Math.min(smallestX, fly.x);
            smallestY = (int)Math.min(smallestY, fly.y);
            largestX = (int)Math.max(largestX, fly.x);
            largestY = (int)Math.max(largestY, fly.y);
        }

        this.mask = new Mask.Rectangle(smallestX, smallestY,
                largestX-smallestX, largestY-smallestY);
    }

    @Override
    public void render(Renderer r) {
        for (int i = flies.size()-1; i>=0; i--) {
            flies.get(i).render(r);
        }
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
