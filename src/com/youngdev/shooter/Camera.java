package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.util.Random;

public class Camera {
    public double cX, cY;
    private double shakeX, shakeY, shakeAmount, mx, my;
    private int width, height;
    public float bluishEffect, bitCrushEffect, blackAndWhiteEffect;
    public GameObject target;
    private Random random;

    public Camera(int width, int height, GameObject target) {
        this.width = width;
        this.height = height;
        this.target = target;
        this.cX = target.x-width/2;
        this.cY = target.y-height/2;
        bluishEffect = 1f;
        bitCrushEffect = 0f;
        blackAndWhiteEffect = 0f;
        this.shakeX = 0d;
        this.shakeY = 0d;
        this.shakeAmount = 0f;

        random = new Random();
    }

    public void update() {
        mx = cX-Main.main.getE().getInput().getRelativeMouseX()+width/2d;
        my = cY-Main.main.getE().getInput().getRelativeMouseY()+height/2d;

        if(Main.startMenuMode) {
            cY -= 0.5;
        } else {
            if (shakeAmount != 0) {
                shakeX = random.nextFloat() * shakeAmount * 2 - shakeAmount;
                shakeY = random.nextFloat() * shakeAmount * 2 - shakeAmount;
                shakeAmount *= 0.9f;
            } else {
                shakeX = 0;
                shakeY = 0;
            }

            cX += ((target.x - cX - width / 2d) * 0.1d);
            cY += ((target.y - cY - height / 2d) * 0.1d);
        }

        bluishEffect += 0.0125f;
        bluishEffect = (float)AdvancedMath.setRange(
                bluishEffect, 0d, 1d);

        bitCrushEffect -= 0.005f;
        bitCrushEffect = (float)AdvancedMath.setRange(
                bitCrushEffect, 0d, 1d);
    }

    public void shake(float amount) {
        shakeAmount += amount;
//        shakeAmount /= amount;
    }

    public void apply(Renderer r) {
//        double cameraX = (cX+width/2d-mx/10d)-width/2d;
//        double cameraY = (cY+height/2d-my/10d)-height/2d;

        r.setCamX((int)cX + (int)shakeX);
        r.setCamY((int)cY + (int)shakeY);
    }
}
