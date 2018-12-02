package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/*
 * Enemy called "Bolt" is fast but weak.
 * */
public class EnemyBolt extends Healable {
    public Healable closestHealable;

    private static final int gotHitTime = 10;
    private int gotHitTimer;
    private boolean isDead;
    private double speedX, speedY, maxSpeed, dir;
    private Random random;

    private ArrayList<UniParticle> particles;

    public EnemyBolt(int x, int y) {
        super(x, y, 32, 32, 50, 3, 12, true, false);
        random = new Random();
        this.dir = random.nextDouble()*360;
        maxSpeed = 3f;
        isDead = false;
        particles = new ArrayList<>();
    }

    @Override
    public void update(Input i) {
        if(isDead) {
            particles.removeIf(o -> {
                if(o.dead) {
                    return true;
                } else {
                    o.update();
                    return false;
                }
            });
            if(particles.size() == 0) {
                dead = true;
            }
            return;
        }

        double minDis = Double.MAX_VALUE;
        Healable closestEnemy = null;
        Iterator<GameObject> it;
        GameObject obj;
        boolean foundHit = false;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            obj = it.next();
            if(obj instanceof Healable && !((Healable) obj).isEnemy) {
                double distance = Math.hypot((x - obj.x), (y - obj.y));
                if(distance <= minDis) {
                    minDis = distance;
                    closestEnemy = (Healable) obj;
                }
            } else if(!foundHit && obj instanceof Arrow) {
                if(((Arrow) obj).shotByFriendly) {
                    if(Math.hypot((x - obj.x), (y - obj.y)) < 12) {
                        health -= 10;
                        gotHitTimer = gotHitTime;

                        double dir = Math.toRadians(Fly.angle(obj.x, obj.y, x, y));
                        x -= Math.cos(dir)*16d;
                        y -= Math.sin(dir)*16d;
                        speedX = 0;
                        speedY = 0;

                        foundHit = true;
                    }
                }
            }
        }

        dir = (dir+Main.toSlowMotion((minDis < 80) ? 4d : 1d)) % 360;

        if(closestEnemy != null) {
            double angle = Math.toDegrees(Math.atan2(y - closestEnemy.y, x - closestEnemy.x))-180;

            speedX += Math.cos(Math.toRadians(angle));
            speedY += Math.sin(Math.toRadians(angle));

            speedX = speedX % maxSpeed;
            speedY = speedY % maxSpeed;

            x += speedX * Main.toSlowMotion(1d);
            y += speedY * Main.toSlowMotion(1d);
        }

        if(!Main.isPixelOnScreen((int)x, (int)y, 2)) {
            dead = true;
        }

        gotHitTimer = Math.max(0, gotHitTimer-1);

        if(health < 1) {
            isDead = true;
            for(int j = 0; j < 8; j++) {
                int xx = (int)(random.nextDouble()*3-1.5d);
                int yy = (int)(random.nextDouble()*3-1.5d);
                Color c = (xx > -8 && xx < 8) ? new Color(32, 32, 160) : new Color(20, 20, 80);

                UniParticle.MovingProcess movingProcess = UniParticle.MovingProcess.create((float)xx, (float)yy, -0.025d);
                UniParticle.FadingProcess fadingProcess = new UniParticle.FadingProcess(255, 10, false);

                particles.add(new UniParticle((int)x+xx, (int)y+yy, 4, true, c, movingProcess, fadingProcess));
            }

            ArrayList<GameObject> tempList = new ArrayList<>();
            for(int k = 0; k < random.nextInt(3)+3; k++) {
                tempList.add(new Coin((int)x, (int)y, random.nextInt(359)));
            }
            Main.main.coins.addAll(tempList);
        }
    }

    @Override
    public void render(Renderer r) {
        if(isDead) {
            particles.forEach(o -> o.render(r));
            return;
        }
        double r1 = 10;
        double r2 = 8;
        double[] xPs1 = new double[]{
                x + Math.cos(Math.toRadians(dir+45))*r1,
                x + Math.cos(Math.toRadians(dir+135))*r1,
                x + Math.cos(Math.toRadians(dir+225))*r1,
                x + Math.cos(Math.toRadians(dir+315))*r1
        };
        double[] yPs1 = new double[]{
                y + Math.sin(Math.toRadians(dir+45))*r1,
                y + Math.sin(Math.toRadians(dir+135))*r1,
                y + Math.sin(Math.toRadians(dir+225))*r1,
                y + Math.sin(Math.toRadians(dir+315))*r1
        };
        double[] xPs2 = new double[]{
                x + Math.cos(Math.toRadians(-dir-45+45))*r2,
                x + Math.cos(Math.toRadians(-dir-45+135))*r2,
                x + Math.cos(Math.toRadians(-dir-45+225))*r2,
                x + Math.cos(Math.toRadians(-dir-45+315))*r2
        };
        double[] yPs2 = new double[]{
                y + Math.sin(Math.toRadians(-dir-45+45))*r2,
                y + Math.sin(Math.toRadians(-dir-45+135))*r2,
                y + Math.sin(Math.toRadians(-dir-45+225))*r2,
                y + Math.sin(Math.toRadians(-dir-45+315))*r2

        };

        float value = (float)gotHitTimer / (float)gotHitTime;

        Color color1 = new Color(
                calcColorParameter(32, 255, value),
                calcColorParameter(32, 0, value),
                calcColorParameter(160, 0, value)
        );

        Color color2 = new Color(
                calcColorParameter(20, 255, value),
                calcColorParameter(20, 0, value),
                calcColorParameter(80, 0, value)
        );

        r.fillPolygon(xPs1, yPs1, 4, color1);
        r.fillPolygon(xPs2, yPs2, 4, color2);
    }

    public static int calcColorParameter(int colorBack, int colorFront, float alpha) {
        return (int) AdvancedMath.setRange((int)(alpha * (float)colorFront) + (int)((1f - alpha) * (float)colorBack), 0, 255);
    }

}
