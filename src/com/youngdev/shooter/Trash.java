package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Trash extends GameObject {

    public int type;
    private Random random;
    ArrayList<UniParticle> particles;
    public final int Type = 12;

    public static final int TYPE_BRANCHES = 1, TYPE_WATER = 2, TYPE_MUD = 3;

    public Trash(int x, int y, int type) {
        super(10, 1);
        this.depth = 1;
        this.x = x;
        this.y = y;
        random = new Random();

//        this.type = random.nextInt(3)+1;
        this.type = type;
//        this.type = TYPE_BRANCHES;

        particles = new ArrayList<>();

        int depth = 0;

        switch (type) {
            case TYPE_BRANCHES:
                depth = 5;
                final Color baseColor0 = new Color(75, 11, 13);
                Color c;

                for(int i = 0; i < random.nextInt(4)+10; i++) {
                    int tone = random.nextInt(20)-10;
                    c = new Color(
                            baseColor0.getRed()+tone,
                            baseColor0.getGreen()+tone,
                            baseColor0.getBlue()+tone
                    );

                    int addX = random.nextInt(50)-25;
                    int addY = random.nextInt(50)-25;

                    double length = random.nextDouble()*4+5;
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

                    particles.add(new UniParticle(x+addX, y+addY, 0,
                            true, c, stepOverProcess));

                    /*{
                        @Override
                        public void render(Renderer r) {
                            r.drawLineWidth(x+x1, y+y1, x+x2, y+y2, width, color);
                        }
                    }*/
                }
                this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
                break;
            case TYPE_WATER:
                depth = 4;
                Color baseColor1 = new Color(150, 150, 230);
                baseColor1 = new Color(
                        UniParticle.calcColorParameter(Main.grassColor.getRed(), baseColor1.getRed(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getGreen(), baseColor1.getGreen(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getBlue(), baseColor1.getBlue(), 0.25f)
                );

                for(int i = 0; i < random.nextInt(4)+10; i++) {
                    int addX = random.nextInt(30)-15;
                    int addY = random.nextInt(30)-15;
                    int size = random.nextInt(8)+14;

                    UniParticle.Process colorRandomizer = new UniParticle.Process() {
                        private Color startColor;
                        private int toneBounds, step;
                        private double currentTone;

                        @Override
                        public void init() {
                            startColor = owner.color;
                            toneBounds = 4;
                            currentTone = 0;
//                            step = random.nextInt(359);
                        }

                        @Override
                        public void render(Renderer r) {

                        }

                        @Override
                        public void update() {
                            step = (step+1)%360;
//                            currentTone = Math.min(toneBounds, Math.max(-toneBounds, ));
                            currentTone = Math.cos(Math.toRadians(step))*toneBounds;
                            owner.color = new Color(
                                    startColor.getRed()+(int)currentTone,
                                    startColor.getGreen()+(int)currentTone,
                                    startColor.getBlue()+(int)currentTone
                            );
                        }
                    };

                    particles.add(new UniParticle(x+addX, y+addY, size, true, baseColor1, colorRandomizer));
                }
                this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
                break;
            case TYPE_MUD:
                depth = 4;
                Color baseColor2 = new Color(99, 27, 23);
                baseColor2 = new Color(
                        UniParticle.calcColorParameter(Main.grassColor.getRed(), baseColor2.getRed(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getGreen(), baseColor2.getGreen(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getBlue(), baseColor2.getBlue(), 0.25f)
                );

                for(int i = 0; i < random.nextInt(4)+10; i++) {
                    int addX = random.nextInt(30)-15;
                    int addY = random.nextInt(30)-15;
                    int size = random.nextInt(8)+14;

                    UniParticle.Process colorRandomizer = new UniParticle.Process() {
                        private Color startColor;
                        private int toneBounds, step;
                        private double currentTone;

                        @Override
                        public void init() {
                            startColor = owner.color;
                            toneBounds = 4;
                            currentTone = 0;
//                            step = random.nextInt(359);
                        }

                        @Override
                        public void render(Renderer r) {

                        }

                        @Override
                        public void update() {
                            step = (step+1)%360;
//                            currentTone = Math.min(toneBounds, Math.max(-toneBounds, ));
                            currentTone = Math.cos(Math.toRadians(step))*toneBounds;
                            owner.color = new Color(
                                    startColor.getRed()+(int)currentTone,
                                    startColor.getGreen()+(int)currentTone,
                                    startColor.getBlue()+(int)currentTone
                            );
                        }
                    };

                    particles.add(new UniParticle(x+addX, y+addY, size, true, baseColor2, colorRandomizer));
                }
                this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
                break;
        }

        // HERE: Fix depth
        this.depth = this.depth*1024+(depth*128+random.nextInt(128));
    }

    @Override
    public void update(Input input) {
//        if(type == TYPE_WATER || type == TYPE_MUD) {
            particles.forEach(UniParticle::update);
//        }
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(o -> o.render(r));
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
