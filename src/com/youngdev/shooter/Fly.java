package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.Iterator;
import java.util.Random;

public class Fly extends GameObject {
    private boolean state; // TRUE - Idle, FALSE - Fly away
    private double targetX, targetY, angle, speed;
    private Random random;

    public Fly(int x, int y) {
        super(3, 15);
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.state = true;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        targetX = x + random.nextInt(32) - 16;
        targetY = y + random.nextInt(32) - 16;
    }

    public Fly(int x, int y, boolean state) {
        super(5, 35);
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.state = state;
        this.random = new Random();
        targetX = x + random.nextInt(32) - 16;
        targetY = y + random.nextInt(32) - 16;

        if(!state) {
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
            angle = angle(closestEnemy.x, closestEnemy.y,
                    x, y) - 180;
        }
    }

    @Override
    public void update(Input input) {
        if(dead) return;
        if(state) {
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

            if (minDis < 100) {
//                dead = true;
                state = false;
                angle = angle(closestEnemy.x, closestEnemy.y,
                        x, y)-180;
            } else {
                if(distance(x, y, targetX, targetY) < 4) {
                    targetX = x + random.nextInt(16) - 8;
                    targetY = y + random.nextInt(16) - 8;
                } else {
                    x += Main.toSlowMotion((targetX - x) * 0.05);
                    y += Main.toSlowMotion((targetY - y) * 0.05);
                }
            }
        } else {
            x += Main.toSlowMotion(Math.cos(Math.toRadians(angle))*speed);
            y += Main.toSlowMotion(Math.sin(Math.toRadians(angle))*speed);
            speed+=0.125;

            Camera camera = Main.main.camera;
            if(!AdvancedMath.inRange(x, y, camera.cX, camera.cY,
                    camera.cX+Main.main.getE().width,
                    camera.cY+Main.main.getE().height)) {
                dead = true;
            }
        }
    }

    @Override
    public void render(Renderer renderer) {
        if(dead) return;
        renderer.fillRectangle((int)x-2, (int)y-2, 4, 4, Color.black);
//        System.out.println("Rendered");
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot((x1 - x2), (y1 - y2));
    }

    public static double angle(double x1, double y1, double x2, double y2) {
        return Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
    }
}
