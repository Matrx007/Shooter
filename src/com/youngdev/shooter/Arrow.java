package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.math.BasicMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Arrow extends GameObject {
    public int dir, wiggleStep;
    private double xD, yD;
    public double addX, addY;
    private ArrayList<UniParticle> particles;
    public boolean shotByFriendly;
    private static final boolean UseParticles = false;
    public Random random;

    public static final float SPEED = 6;

    public Arrow(int x, int y, int dir) {
        super(1, 13);
        this.x = x;
        this.y = y;
        this.xD = x;
        this.yD = y;
        this.dir = dir;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        wiggleStep = random.nextInt(359);
        if(UseParticles)
            particles = new ArrayList<>();

        mask = new Mask.Rectangle(x-8, y-8, 16, 16);
    }

    @Override
    public void update(Input input) {

        wiggleStep = Main.toSlowMotion((wiggleStep+16)%359);

        xD += Main.toSlowMotion(addX);
        yD += Main.toSlowMotion(addY);

        addX -= Math.signum(addX)*Main.toSlowMotion(0.25);
        addY -= Math.signum(addY)*Main.toSlowMotion(0.25);

        xD += Main.toSlowMotion(Math.cos(Math.toRadians(dir))*SPEED);
        yD += Main.toSlowMotion(Math.sin(Math.toRadians(dir))*SPEED);


        x = (int)xD;
        y = (int)yD;

//        System.out.println("X: "+xx);
//        System.out.println("Y: "+yy);


        if(UseParticles) {
            int addWiggleX = (int) (Math.cos(Math.toRadians(wiggleStep)) * 4);
            int addWiggleY = (int) (Math.sin(Math.toRadians(wiggleStep)) * 4);
            if (random.nextBoolean()) {
                int dir = random.nextInt(359);
                int tone = random.nextInt(20);
                int sDir = random.nextInt(359);
                int sDistance = random.nextInt(4);
                int xx = (int) x + (int) (Math.cos(Math.toRadians(sDir)) * sDistance) + addWiggleX;
                int yy = (int) y + (int) (Math.sin(Math.toRadians(sDir)) * sDistance) + addWiggleY;
                Color color = new Color(tone, tone, tone);
                int fadingSpeed = random.nextInt(8) + 20;
                UniParticle.FadingProcess fadingProcess = new UniParticle.FadingProcess(255, fadingSpeed, true);
                particles.add(new UniParticle(xx, yy, random.nextBoolean() ? 4 : 2, true, color, fadingProcess));
//            leaf.add(new Particle(xx, yy, 4, dir, tone, Color.red));
            }

            // HERE: Update leaf and remove dead ones
            particles.forEach(UniParticle::update);
            particles.removeIf(particle -> particle.dead);
        }
    }

    @Override
    public void render(Renderer r) {
//        r.drawLine(Main.main.player.xx, Main.main.player.yy, xx, yy, Color.black);
//        r.fillRectangle(xx, yy, 16, 16, Color.red);

        if(UseParticles)
            particles.forEach(p -> p.render(r));
        else {
            double[] xPs1 = new double[]{
                    x + Math.cos(Math.toRadians(dir+45))*4,
                    x + Math.cos(Math.toRadians(dir+135))*4,
                    x + Math.cos(Math.toRadians(dir+225))*4,
                    x + Math.cos(Math.toRadians(dir+315))*4
            };
            double[] yPs1 = new double[]{
                y + Math.sin(Math.toRadians(dir+45))*4,
                y + Math.sin(Math.toRadians(dir+135))*4,
                y + Math.sin(Math.toRadians(dir+225))*4,
                y + Math.sin(Math.toRadians(dir+315))*4
            };
            double[] xPs2 = new double[]{
                x + Math.cos(Math.toRadians(dir+45))*2,
                x + Math.cos(Math.toRadians(dir+135))*2,
                x + Math.cos(Math.toRadians(dir+225))*2,
                x + Math.cos(Math.toRadians(dir+315))*2
            };
            double[] yPs2 = new double[]{
                y + Math.sin(Math.toRadians(dir+45))*2,
                y + Math.sin(Math.toRadians(dir+135))*2,
                y + Math.sin(Math.toRadians(dir+225))*2,
                y + Math.sin(Math.toRadians(dir+315))*2

            };
            r.fillPolygon(xPs1, yPs1, 4, new Color(255, 200, 40));
            r.fillPolygon(xPs2, yPs2, 4, new Color(255, 255, 240));
        }
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
