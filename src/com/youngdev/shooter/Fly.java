package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.Random;

public class Fly extends WorldObject {
    public boolean wandering; // TRUE - Idle, FALSE - Fly away
    private double targetX, targetY, speed;
    public double angle;
    public final int Type = 5;
    private int minDistanceToFlyAway, flyCounter;
    public double direction;
    private double prevX, prevY;
    private static long lastSound;

    public Fly(int x, int y) {
        super(3, 15, 5);
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.wandering = true;

        targetX = x + random.nextInt(32) - 16;
        targetY = y + random.nextInt(32) - 16;

        minDistanceToFlyAway = 75 + random.nextInt(75);

        this.mask = new Mask.Rectangle(x-8, y-8, 16, 16);
    }

    public Fly(int x, int y, boolean wander) {
        super(5, 35, 5);
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.wandering = wander;
        if(!wander) flyAwaySound();
        this.random = new Random();
        targetX = x + random.nextInt(32) - 16;
        targetY = y + random.nextInt(32) - 16;
        flyCounter = 0;

        minDistanceToFlyAway = 75 + random.nextInt(75);
        this.mask = new Mask.Rectangle(x-8, y-8, 16, 16);
    }

    @Override
    public void update(Input input) {
        if(dead) return;

        if(wandering) {
            Player player = Main.main.player;
            if (AdvancedMath.inRange(player.x, player.y,
                    x - 80,y - 80,
                    160, 160)) {
                wandering = false;
                angle = angle(player.x, player.y, x, y)-180;
                if(!Main.main.visibleAreaMask.isColliding((int)x, (int)y))
                    dead = true;
                else flyAwaySound();
            } else for (WorldObject o : Main.main.visibleChunkEntities) {
                if(o instanceof Rabbit)
                    if (AdvancedMath.inRange(o.x, o.y, x - 80,
                            y - 80, 160, 160)) {
                        wandering = false;
                        angle = angle(o.x, o.y, x, y)-180;
                        if(!Main.main.visibleAreaMask.isColliding((int)x, (int)y))
                            dead = true;
                        else flyAwaySound();
                        break;
                    }
            }
        }

        if(wandering) {
            if(distance(x, y, targetX, targetY) < 4) {
                targetX = x + random.nextInt(64) - 32;
                targetY = y + random.nextInt(64) - 32;
            } else {
                x += Main.toSlowMotion((targetX - x) * 0.025);
                y += Main.toSlowMotion((targetY - y) * 0.025);
            }
        } else {
            x += Main.toSlowMotion(Math.cos(Math.toRadians(angle))*speed);
            y += Main.toSlowMotion(Math.sin(Math.toRadians(angle))*speed);
            speed += 0.0625;

            if(!Main.main.visibleAreaMask.isColliding((int)x, (int)y)) {
                dead = true;
            }
        }

        if(SpeedController.calcSpeed() != 0)
            direction = Fly.angle(prevX, prevY, x, y);

        prevX = x;
        prevY = y;
    }

    @Override
    public void render(Renderer r) {
        if(dead) return;
//        r.fillRectangle((int)scoreX-2, (int)scoreY-2, 4, 4, Color.black);
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

    private void flyAwaySound() {
        if(Main.notInGame) return;

        long time = System.currentTimeMillis();
        if(time-lastSound > 150) {
            lastSound = time;
            double distance = distance(x, y,
                    Main.main.player.x, Main.main.player.y);
            float gain = -Math.max(20f, 80f / (float) distance * 15f);
            Main.main.soundManager.playSound("flyAway", gain+5f);
        }
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

    static void drawPoly(double[][] points, Color color, Renderer r) {
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
        r.drawPolygon(xPoints, yPoints, points.length, color);
    }

    static double[] rotatePoint(double x, double y, double anchorX,
                                       double anchorY, double degrees) {
        double xx = (x - anchorX) * Math.cos(degrees * Math.PI / 180d) - (y - anchorY) * Math.sin(degrees * Math.PI / 180d) + anchorX;
        double yy = (x - anchorX) * Math.sin(degrees * Math.PI / 180d) + (y - anchorY) * Math.cos(degrees * Math.PI / 180d) + anchorY;
        return new double[]{xx, yy};
    }

    public void flyAway(boolean force) {
        if(force || Main.main.flySoundCounter < Main.main.flySounds) {
            Main.main.soundManager.playSound(
                    "beeFlyingAway", -20f);
            Main.main.flySoundCounter++;
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
                    double distance = Math.hypot((scoreX - obj.scoreX), (scoreY - obj.scoreY));
                    if(distance <= minDis) {
                        minDis = distance;
                        closestEnemy = (Healable) obj;
                    }
                }
            }

            if (minDis < minDistanceToFlyAway && closestEnemy != null) {
                wandering = false;
                angle = angle(closestEnemy.scoreX, closestEnemy.scoreY,
                        scoreX, scoreY)-180+random.nextInt(90)-45;
                if(Main.main.isPixelOnScreen((int)scoreX, (int)scoreY)) {
                    Main.main.flies.forEach(f -> {
                        if(Fly.distance(scoreX, scoreY, f.scoreX, f.scoreY) < 64) {
                            flySoundCounter++;
                        }
                    });
                    if(flySoundCounter > 0)
                        flyAway(false);
                    else flyAway(true);
                    flySoundCounter = 0;
                }
            } else
* */
