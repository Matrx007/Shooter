package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.input.Input;
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
        this.depth = 15*1024+random.nextInt(1024);
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
                fly.state = false;
                fly.direction = direction;
            }
        }
        this.depth = 15*1024+random.nextInt(1024);
    }

    @Override
    public void update(Input i) {
        double minDis = Double.MAX_VALUE;
        Healable closestEnemy = null;
        Iterator<GameObject> it;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            GameObject obj = it.next();
            if(obj instanceof Healable && !((Healable) obj).isEnemy) {
                double distance = Math.hypot((x - obj.x), (y - obj.y));
                if(distance <= minDis) {
                    minDis = distance;
                    closestEnemy = (Healable) obj;
                }
            }
        }

        boolean flyAway = false;
        double direction = 0;

        if(minDis < range && closestEnemy != null) {
            direction = Fly.distance(
                    closestEnemy.x, closestEnemy.y,
                    x, y) - 180;
            flyAway = true;
        }

        for (int j = flies.size()-1; j>=0; j--) {
            Fly fly = flies.get(j);
            if(fly.state)
                if(flyAway) {
                    fly.state = false;
                    fly.angle = direction+random.nextInt(90)-45;
                }
            fly.update(i);
        }
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
