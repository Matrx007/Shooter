package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Branches extends WorldObject {
    public ArrayList<UniParticle> particles;
    public final int Type = 12;
    private int smallestX, smallestY, largestX, largestY;
    private Color baseColor0;
    private Random random;
    private AABBCollisionManager cm;

    public Branches(int x, int y) {
        super(15, 1, 14);

        this.x = x;
        this.y = y;

        random = new Random();

        particles = new ArrayList<>();

        // HERE: Generate branches
        baseColor0 = new Color(75, 11, 13);
        for(int i = 0; i < random.nextInt(10)+10+50+
                ((random.nextInt(5)==1) ? 50 : 0); i++) {
            double angle = random.nextDouble()*360d;
            double distance = calcGaussian(random.nextDouble(), 4)*70;

            double xx = Math.cos(Math.toRadians(angle))*distance;
            double yy = Math.sin(Math.toRadians(angle))*distance;

            particles.add(createParticle((int)xx, (int)yy));
        }
        this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
        cm = new AABBCollisionManager(this, Main.collisionMap);
    }

    public UniParticle createParticle(int addX, int addY) {
        int tone = random.nextInt(20)-10;
        Color c = new Color(
                baseColor0.getRed()+tone,
                baseColor0.getGreen()+tone,
                baseColor0.getBlue()+tone
        );

        double length = random.nextDouble()*4d+5;
        double angle = Math.toRadians(random.nextInt(359));
        int x1 = (int)(Math.cos(angle)*length);
        int y1 = (int)(Math.sin(angle)*length);
        int x2 = (int)(Math.cos(angle-180)*length);
        int y2 = (int)(Math.sin(angle-180)*length);
        int width = random.nextInt(2)+2;

        UniParticle.Process stepOverProcess = new UniParticle.Process() {
            private double speedX, speedY, xx, yy, angle, targetAngle;
            private double xx1, yy1, xx2, yy2;
            private Color color;
            private boolean prevCollision;

            @Override
            public void init() {
                speedX = 0;
                speedY = 0;
                this.xx = owner.x;
                this.yy = owner.y;
                this.color = new Color(
                        baseColor0.getRed()+tone,
                        baseColor0.getGreen()+tone,
                        baseColor0.getBlue()+tone
                );
                this.xx1 = x1;
                this.yy1 = y1;
                this.xx2 = x2;
                this.yy2 = y2;
                this.angle = random.nextInt(359);
                targetAngle = angle;
                prevCollision = false;
            }

            @Override
            public void render(Renderer r) {
                r.drawLineWidth(xx+(int)xx1, yy+(int)yy1,
                        xx+(int)xx2, yy+(int)yy2, width, color);
            }

            @Override
            public void update() {
//                System.out.println("xx = " + xx);
                double prevAngle = angle;
                speedX *= 0.5;
                speedY *= 0.5;

                boolean found = false;
                boolean move = random.nextBoolean();
                Player player = Main.main.player;
                if(Fly.distance(owner.x, owner.y, player.x, player.y) < 16d) {
                    if(move) {
                        speedX = Math.cos(Math.toRadians(
                                Fly.angle(player.x, player.y, owner.x, owner.y) - 180));
                        speedY = Math.sin(Math.toRadians(
                                Fly.angle(player.x, player.y, owner.x, owner.y) - 180));
                    }
                    found = true;
                } else {
                    for (GameObject entity : Main.main.entities) {
                        if(entity instanceof Healable) {
                            if (Fly.distance(entity.x, entity.y, owner.x, owner.y) < 16d) {
                                if(move) {
                                    speedX = Math.cos(Math.toRadians(
                                            Fly.angle(entity.x, entity.y, owner.x, owner.y) - 180));
                                    speedY = Math.sin(Math.toRadians(
                                            Fly.angle(entity.x, entity.y, owner.x, owner.y) - 180));
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if(found && !prevCollision) {
                    targetAngle += random.nextInt(90)-45;
                }

                prevCollision = found;

                angle += (targetAngle - angle) *0.1d;

                xx += Main.toSlowMotion(speedX);
                yy += Main.toSlowMotion(speedY);

                owner.x = (int)xx;
                owner.y = (int)yy;

                if(angle != prevAngle) {
                    xx1 = (Math.cos(Math.toRadians(angle))*length);
                    yy1 = (Math.sin(Math.toRadians(angle))*length);
                    xx2 = (Math.cos(Math.toRadians(angle-180))*length);
                    yy2 = (Math.sin(Math.toRadians(angle-180))*length);
                }
            }
        };

        return new UniParticle((int)x+addX, (int)y+addY, 0,
                true, c, stepOverProcess);
    }

    @Override
    public void update(Input i) {
        smallestX=Integer.MAX_VALUE;
        smallestY=Integer.MAX_VALUE;
        largestX=Integer.MIN_VALUE;
        largestY=Integer.MIN_VALUE;

        particles.forEach((p) -> {
            smallestX = Math.min(smallestX, p.x);
            smallestY = Math.min(smallestY, p.y);
            largestX = Math.max(largestX, p.x);
            largestY = Math.max(largestY, p.y);
//            System.out.println("p.x = " + p.x);
//            System.out.println("p.y = " + p.y);
            p.update();
//            System.out.println("====");
//            System.out.println("p.x = " + p.x);
//            System.out.println("p.y = " + p.y);
        });

        mask = new Mask.Rectangle(
                (int)smallestX-10,
                (int)smallestY-10,
                (int)largestX-smallestX+20,
                (int)largestY-smallestY+20);
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(o -> {
            o.render(r);
        });

        if(Main.main.showDebugInfo) {
            r.drawRectangle(mask.x, mask.y,
                    ((Mask.Rectangle) mask).w, ((Mask.Rectangle) mask).h,
                    Color.blue);
        }
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    public static double calcGaussian(double x, int pow) {
        return Math.pow(Math.sin(x+ Math.PI/2), pow);
    }
}
