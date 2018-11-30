package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Trash extends GameObject {

    private int type;
    private Random random;
    private ArrayList<UniParticle> particles;

    public static final int TYPE_BRANCHES = 1, TYPE_WATER = 2, TYPE_MUD = 3;

    public Trash(int x, int y) {
        super(10, 5);
        this.x = x;
        this.y = y;
        random = new Random();

        // HERE: Fix depth
        this.depth = random.nextInt(1023)+depth*1024;

        this.type = random.nextInt(3)+1;
//        this.type = TYPE_BRANCHES;

        particles = new ArrayList<>();

        switch (type) {
            case TYPE_BRANCHES:
                Color baseColor = new Color(75, 11, 13);
                Color c;

                for(int i = 0; i < random.nextInt(4)+10; i++) {
                    int tone = random.nextInt(20)-10;
                    c = new Color(
                            baseColor.getRed()+tone,
                            baseColor.getGreen()+tone,
                            baseColor.getBlue()+tone
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

                    particles.add(new UniParticle(x+addX, y+addY, 0, true, c) {
                        @Override
                        public void render(Renderer r) {
                            r.drawLineWidth(x+x1, y+y1, x+x2, y+y2, width, color);
                        }
                    });
                }
                this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
                break;
            case TYPE_WATER:
                baseColor = new Color(150, 150, 230);
                baseColor = new Color(
                        UniParticle.calcColorParameter(Main.grassColor.getRed(), baseColor.getRed(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getGreen(), baseColor.getGreen(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getBlue(), baseColor.getBlue(), 0.25f)
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

                    particles.add(new UniParticle(x+addX, y+addY, size, true, baseColor, colorRandomizer));
                }
                this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
                break;
            case TYPE_MUD:
                baseColor = new Color(99, 27, 23);
                baseColor = new Color(
                        UniParticle.calcColorParameter(Main.grassColor.getRed(), baseColor.getRed(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getGreen(), baseColor.getGreen(), 0.25f),
                        UniParticle.calcColorParameter(Main.grassColor.getBlue(), baseColor.getBlue(), 0.25f)
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

                    particles.add(new UniParticle(x+addX, y+addY, size, true, baseColor, colorRandomizer));
                }
                this.mask = new Mask.Rectangle(x-30, y-30, 60, 60);
                break;
        }
    }

    @Override
    public void update(Input input) {
        if(type == TYPE_WATER || type == TYPE_MUD) {
            particles.forEach(UniParticle::update);
        }
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
