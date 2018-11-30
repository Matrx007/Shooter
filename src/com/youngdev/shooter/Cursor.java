package com.youngdev.shooter;

import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Cursor {
    public ArrayList<UniParticle> particles;
    private Random random;

    public Cursor() {
        particles = new ArrayList<>();
        random = new Random();
    }

    public void update(Input i) {
        createParticle(i);

        if(i.isButtonDown(1)) {
//            leaf.clear();
//            for(int j = 0; j < 10; j++) {
//
//            }
//            leaf.forEach(p -> p.impulse(i.getRelativeMouseX(), i.getRelativeMouseY(), 0.5f));
        }

        particles.forEach(UniParticle::update);
        particles.removeIf(particle -> particle.dead);
    }

    private void createParticle(Input i) {
        int x = i.getRelativeMouseX();
        int y = i.getRelativeMouseY();
        int tone = random.nextInt(20);
        int dir = random.nextInt(359);
        int distance = random.nextInt(8);
        int xx = x + (int)(Math.cos(Math.toRadians(dir))*distance);
        int yy = y + (int)(Math.sin(Math.toRadians(dir))*distance);
        int alpha = random.nextInt(32)+213;
        alpha = 255;
        int alphaSpeed = 16;
        int size = (random.nextInt(2)+1)*2;
        Color color = new Color(164+tone, 170+tone, 46+tone);
        UniParticle.FadingProcess fadingProcess = new UniParticle.FadingProcess(alpha, alphaSpeed, true);
        particles.add(new UniParticle(xx, yy, size, false, color, fadingProcess));


        /*
        int xx = i.getRelativeMouseX();
        int yy = i.getRelativeMouseY();
        int dir = random.nextInt(359);
        float speed = random.nextInt(10)/10f+1f;
        int tone = random.nextInt(20);
        int sDir = random.nextInt(359);
        int sDistance = random.nextInt(8);
        int xx = xx + (int)(Math.cos(Math.toRadians(sDir))*sDistance);
        int yy = yy + (int)(Math.sin(Math.toRadians(sDir))*sDistance);
        leaf.add(new Particle(xx, yy, 4, dir, 0, tone));*/
    }

    public void render(Renderer r) {
        particles.forEach(p -> p.render(r));
    }
}
