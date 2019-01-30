package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;
import java.util.Iterator;
import java.util.Random;

public class Fly extends WorldObject {
    public boolean state; // TRUE - Idle, FALSE - Fly away
    private double targetX, targetY, speed;
    public double angle;
    private Random random;
    public final int Type = 5;
    private int minDistanceToFlyAway, flyCounter;
    public double direction;
    private double prevX, prevY;

    public Fly(int x, int y) {
        super(3, 15, 5);
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.state = true;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        targetX = x + random.nextInt(32) - 16;
        targetY = y + random.nextInt(32) - 16;

        minDistanceToFlyAway = 75 + random.nextInt(75);
    }

    public Fly(int x, int y, boolean state) {
        super(5, 35, 5);
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.state = state;
        this.random = new Random();
        targetX = x + random.nextInt(32) - 16;
        targetY = y + random.nextInt(32) - 16;
        flyCounter = 0;

        minDistanceToFlyAway = 75 + random.nextInt(75);
    }

    @Override
    public void update(Input input) {
        if(dead) return;
        if(state) {
            {
                if(distance(x, y, targetX, targetY) < 4) {
                    targetX = x + random.nextInt(64) - 32;
                    targetY = y + random.nextInt(64) - 32;
                } else {
                    x += Main.toSlowMotion((targetX - x) * 0.025);
                    y += Main.toSlowMotion((targetY - y) * 0.025);
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

        direction = Fly.angle(prevX, prevY, x, y);

        prevX = x;
        prevY = y;
    }

    @Override
    public void render(Renderer r) {
        if(dead) return;
//        r.fillRectangle((int)x-2, (int)y-2, 4, 4, Color.black);
        double[][] points;

        // Body
        points = new double[][]{
                rotatePoint(x-2, y-2, x, y, direction-90),
                rotatePoint(x+2, y-2, x, y, direction-90),
                rotatePoint(x+2, y+6, x, y, direction-90),
                rotatePoint(x-2, y+6, x, y, direction-90)
        };
        fillPoly(points, Color.black, r);

        // Stripes
        points = new double[][]{
                rotatePoint(x-2, y+5, x, y, direction-90),
                rotatePoint(x+2, y+5, x, y, direction-90),
                rotatePoint(x+2, y+4, x, y, direction-90),
                rotatePoint(x-2, y+4, x, y, direction-90)
        };
        fillPoly(points, Color.yellow, r);
        points = new double[][]{
                rotatePoint(x-2, y+3, x, y, direction-90),
                rotatePoint(x+2, y+3, x, y, direction-90),
                rotatePoint(x+2, y+2, x, y, direction-90),
                rotatePoint(x-2, y+2, x, y, direction-90)
        };
        fillPoly(points, Color.yellow, r);
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

    static void fillPoly(double[][] points, Color color, Renderer r) {
        double[] xPoints = new double[points.length]/*{
                points[0][0],
                points[1][0],
                points[2][0],
                points[3][0]
        }*/;
        double[] yPoints = new double[points.length]/*{
            points[0][1],
                    points[1][1],
                    points[2][1],
                    points[3][1]
        }*/;

        for(int i = 0; i < points.length; i++) {
            xPoints[i] = points[i][0];
            yPoints[i] = points[i][1];
        }
        r.fillPolygon(xPoints, yPoints, points.length, color);
    }

    static double[] rotatePoint(double x, double y, double anchorX,
                                       double anchorY, double degrees) {
        double xx = (x - anchorX) * Math.cos(degrees * Math.PI / 180) - (y - anchorY) * Math.sin(degrees * Math.PI / 180) + anchorX;
        double yy = (x - anchorX) * Math.sin(degrees * Math.PI / 180) + (y - anchorY) * Math.cos(degrees * Math.PI / 180) + anchorY;
        return new double[]{xx, yy};
    }

    public void flyAway(boolean force) {
        if(force || Main.main.flyCounter < Main.main.flySounds) {
            Main.main.soundManager.playSound(
                    "beeFlyingAway", -20f);
            Main.main.flyCounter++;
        }
    }
}

/*
* double minDis = Double.MAX_VALUE;
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

            if (minDis < minDistanceToFlyAway && closestEnemy != null) {
                state = false;
                angle = angle(closestEnemy.x, closestEnemy.y,
                        x, y)-180+random.nextInt(90)-45;
                if(Main.main.isPixelOnScreen((int)x, (int)y)) {
                    Main.main.flies.forEach(f -> {
                        if(Fly.distance(x, y, f.x, f.y) < 64) {
                            flyCounter++;
                        }
                    });
                    if(flyCounter > 0)
                        flyAway(false);
                    else flyAway(true);
                    flyCounter = 0;
                }
            } else
* */
