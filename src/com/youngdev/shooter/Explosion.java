package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Explosion extends GameObject {
    private Random random;
    private int size;
    private ArrayList<UniParticle> particles;

    public Explosion(int x, int y, int depth, boolean useAlpha, int range) {
        super(4, depth);
        this.x = x;
        this.y = y;

        this.size = 0;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        particles = new ArrayList<>();

        // HERE: Create particles
        int numParticles = 120;
        for(int i = 0; i < numParticles; i++) {
            double angle = random.nextDouble()*360;
            double speed = random.nextDouble()*2f+2f;
            double spdX = (Math.cos(Math.toRadians(angle))*speed);
            double spdY = (Math.sin(Math.toRadians(angle))*speed);
            UniParticle.MovingProcess movingProcess = UniParticle.MovingProcess.create(
                    angle, (float)speed, 0.5
            );
            UniParticle.ColorChangeEffect colorChangeEffect = new UniParticle.ColorChangeEffect(
                    new Color( 250, 230, 80), new Color(180, 0, 0), 0, random.nextInt(20)+10
            );
            particles.add(new UniParticle(x, y, 4, true, Color.black, movingProcess, colorChangeEffect));
        }

        this.mask = new Mask.Rectangle(x-128, y-128, 256, 256);
    }

    @Override
    public void update(Input input) {
        size = 0;
        particles.removeIf(p -> {
            if(p.dead) {
                return true;
            } else {
                p.update();
                size++;
                return false;
            }
        });
        if(size == 0) {
            dead = true;
        }
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(p -> p.render(r));
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
