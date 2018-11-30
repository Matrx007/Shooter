package com.youngdev.shooter;

import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

public class Particle {
    private int x, y, dir, alpha;
    int size, alphaSpeed;
    private double xD, yD;
    private float speed;
    boolean dead;

    Color color, baseColor = new Color(164, 170, 46);

    public Particle(int x, int y, int size, int dir, float speed, int tone, int alphaSpeed) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = speed;
        this.size = size;
        this.dead = false;
        this.alphaSpeed = alphaSpeed;
        xD = x;
        yD = y;
        alpha = 255;
        color = new Color(baseColor.getRed()+tone,
                baseColor.getGreen()+tone,
                baseColor.getBlue()+tone);
    }

    public Particle(int x, int y, int size, int dir, float speed, int tone) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = speed;
        this.size = size;
        this.dead = false;
        this.alphaSpeed = 16;
        xD = x;
        yD = y;
        alpha = 255;
        color = new Color(baseColor.getRed()+tone,
                baseColor.getGreen()+tone,
                baseColor.getBlue()+tone);
    }

    public Particle(int x, int y, int size, int dir, int tone) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = 0;
        this.size = size;
        this.dead = false;
        this.alphaSpeed = 16;
        xD = x;
        yD = y;
        alpha = 255;
        color = new Color(baseColor.getRed()+tone,
                baseColor.getGreen()+tone,
                baseColor.getBlue()+tone);
    }

    public Particle(int x, int y, int size, int dir, float speed, int tone, int alphaSpeed, Color color) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = speed;
        this.size = size;
        this.dead = false;
        this.alphaSpeed = alphaSpeed;
        this.baseColor = color;
        xD = x;
        yD = y;
        alpha = 255;
        this.color = new Color(Math.max(0, Math.min(255, baseColor.getRed()+tone)),
                Math.max(0, Math.min(255, baseColor.getGreen()+tone)),
                Math.max(0, Math.min(255, baseColor.getBlue()+tone)));
    }

    public Particle(int x, int y, int size, int dir, float speed, int tone, Color color) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = speed;
        this.size = size;
        this.dead = false;
        this.alphaSpeed = 16;
        this.baseColor = color;
        xD = x;
        yD = y;
        alpha = 255;
        this.color = new Color(Math.max(0, Math.min(255, baseColor.getRed()+tone)),
                Math.max(0, Math.min(255, baseColor.getGreen()+tone)),
                Math.max(0, Math.min(255, baseColor.getBlue()+tone)));
    }

    public Particle(int x, int y, int size, int dir, int tone, Color color) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = 0;
        this.size = size;
        this.dead = false;
        this.alphaSpeed = 16;
        this.baseColor = color;
        xD = x;
        yD = y;
        alpha = 255;
        this.color = new Color(Math.max(0, Math.min(255, baseColor.getRed()+tone)),
                Math.max(0, Math.min(255, baseColor.getGreen()+tone)),
                Math.max(0, Math.min(255, baseColor.getBlue()+tone)));
    }

    public void impulse(int x, int y, float strength) {
        dir = (int)((float)Math.toDegrees(Math.atan2(this.y-y, this.x-x)) + 90.0f);
        speed = strength;
        alpha = 255;
    }

    public void update() {
        if(dead) return;

        if(speed != 0) {
            xD += Math.cos(Math.toRadians(dir)) * speed;
            yD += Math.sin(Math.toRadians(dir)) * speed;

            speed *= 0.995;
        }

        alpha -= alphaSpeed;

        if(alpha < 0) {
            dead = true;
            return;
        }

        this.color = new Color(
                calcColorParameter(Main.grassColor.getRed(), baseColor.getRed(), alpha/255f),
                calcColorParameter(Main.grassColor.getGreen(), baseColor.getGreen(), alpha/255f),
                calcColorParameter(Main.grassColor.getBlue(), baseColor.getBlue(), alpha/255f));
//            System.out.println(color);

        x = (int)xD;
        y = (int)yD;
    }

    public static int calcColorParameter(int colorBack, int colorFront, float alpha) {
        return (int)(alpha * colorFront + (1 - alpha) * colorBack);
    }

    public void render(Renderer r) {
        if(dead) return;
        r.fillRectangle(x-size/2, y-size/2, size, size, color);
    }
}
