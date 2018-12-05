package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.util.Random;

public class Camera {
    public double cX, cY;
    private double shakeX, shakeY, shakeAmount;
    private int width, height;
    public float bluishEffect;
    public GameObject target;
    private Random random;

    public Camera(int width, int height, GameObject target) {
        this.width = width;
        this.height = height;
        this.target = target;
        this.cX = target.x-width/2;
        this.cY = target.y-height/2;
        bluishEffect = 1f;
        this.shakeX = 0d;
        this.shakeY = 0d;
        this.shakeAmount = 0f;

        random = new Random();
    }

    public void update() {
        if(Main.startMenuMode) {
            cY -= 0.5;
        } else {
            if (shakeAmount != 0) {
                shakeX = Main.toSlowMotion(random.nextFloat() * shakeAmount * 2 - shakeAmount);
                shakeY = Main.toSlowMotion(random.nextFloat() * shakeAmount * 2 - shakeAmount);
                shakeAmount *= Main.toSlowMotion(0.9f);
            } else {
                shakeX = 0;
                shakeY = 0;
            }

            cX += Main.toSlowMotion((target.x - cX - width / 2d) * 0.1d);
            cY += Main.toSlowMotion((target.y - cY - height / 2d) * 0.1d);
        }
    }

    public void shake(float amount) {
        shakeAmount += amount;
//        shakeAmount /= amount;
    }

    public void apply(Renderer r) {
        r.setCamX((int)cX + (int)shakeX);
        r.setCamY((int)cY + (int)shakeY);
    }
}
