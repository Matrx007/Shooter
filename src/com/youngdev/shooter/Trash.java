package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.youngdev.shooter.multiPlayerManagement.WorldObject;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Trash extends WorldObject {

    public int type;
    private Random random;
    ArrayList<UniParticle> particles;
    public final int Type = 12;

    public static final int TYPE_BRANCHES = 1, TYPE_WATER = 2, TYPE_MUD = 3;

    public Trash(int x, int y, int type) {
        super(10, 1, 12);
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
                type = random.nextBoolean() ? TYPE_WATER : TYPE_MUD;
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
        this.depth = this.depth*1024+(depth*64+random.nextInt(64));
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
