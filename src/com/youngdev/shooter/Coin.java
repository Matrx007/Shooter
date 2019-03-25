package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

public class Coin extends WorldObject {

    public double speed, rotation, rotationSpeed, xD, yD, angle, pickupSpeedAdd;
    public final int Type = 2;
    public boolean pickable;
    private double multiplier;
    private static long lastCoinPickupSound;
    private AABBCollisionManager cm;

    public Coin(int x, int y, double angle) {
        super(2, 9, 2);

        this.x = x;
        this.y = y;
        this.xD = x;
        this.yD = y;
        this.angle = angle;
        this.pickable = false;
        pickupSpeedAdd = 0;

//        this.speed = 0;
        this.multiplier = 0.9+random.nextDouble()/20;
        this.speed = random.nextDouble()*3+1d;
        this.rotationSpeed = 8;
        this.rotation = random.nextInt(359);

        this.mask = new Mask.Rectangle(x-8, y-8, 16, 16);
        this.aabbComponent = new AABBComponent(this.mask);
        cm = new AABBCollisionManager(this, Main.collisionMap);
    }

    @Override
    public void update(Input input) {
        this.speed *= multiplier;
        this.rotationSpeed *= 0.95;
        this.rotation += rotationSpeed;

        if(Fly.distance(xD, yD, Main.main.player.x, Main.main.player.y) < 32d && pickable) {
            if(Fly.distance(xD, yD, Main.main.player.x, Main.main.player.y) < 8d) {
                dead = true;
                Main.main.player.lastCoinX = xD;
                Main.main.player.lastCoinY = yD;
                Main.main.player.coinOverlayAlpha = 1d;
                Main.main.player.money += 2;
                if(System.currentTimeMillis()-lastCoinPickupSound > 50) {
                    lastCoinPickupSound = System.currentTimeMillis();
                    Main.main.soundManager.playSound("pickup");
                    Main.main.coinSoundCounter++;
                }
            } else {
                angle = Fly.angle(xD, yD, Main.main.player.x, Main.main.player.y)-180;
                speed = 4d+pickupSpeedAdd;
                pickupSpeedAdd += 0.025;
            }
        }

        if(this.speed < 0.25) pickable = true;

        double addX = Math.cos(Math.toRadians(angle))*speed*SpeedController.calcSpeed();
        double addY = Math.sin(Math.toRadians(angle))*speed*SpeedController.calcSpeed();

        if(Main.collisionMap.collisionWithExcept(
                mask, aabbComponent)) {
            x += addX;
            y += addY;
            mask.move(addX, addY);
//            cm.unstuck();
        } else {
            cm.move(addX, addY);
            xD = x;
            yD = y;
        }
    }

    @Override
    public void render(Renderer r) {
//        r.fillRectangle(scoreX, scoreY, 8, 8, Color.yellow);

        int x1 = (int)(xD+Math.cos(Math.toRadians(rotation))*5d);
        int y1 = (int)(yD+Math.sin(Math.toRadians(rotation))*5d);
        int x2 = (int)(xD+Math.cos(Math.toRadians(rotation-90))*5d);
        int y2 = (int)(yD+Math.sin(Math.toRadians(rotation-90))*5d);
        int x3 = (int)(xD+Math.cos(Math.toRadians(rotation-180))*5d);
        int y3 = (int)(yD+Math.sin(Math.toRadians(rotation-180))*5d);
        int x4 = (int)(xD+Math.cos(Math.toRadians(rotation-270))*5d);
        int y4 = (int)(yD+Math.sin(Math.toRadians(rotation-270))*5d);

        r.fillPolygon(new int[]{x1, x2, x3, x4}, new int[]{y1, y2, y3, y4}, Color.yellow);

        x1 = (int)(xD+Math.cos(Math.toRadians(rotation))*3d);
        y1 = (int)(yD+Math.sin(Math.toRadians(rotation))*3d);
        x2 = (int)(xD+Math.cos(Math.toRadians(rotation-90))*3d);
        y2 = (int)(yD+Math.sin(Math.toRadians(rotation-90))*3d);
        x3 = (int)(xD+Math.cos(Math.toRadians(rotation-180))*3d);
        y3 = (int)(yD+Math.sin(Math.toRadians(rotation-180))*3d);
        x4 = (int)(xD+Math.cos(Math.toRadians(rotation-270))*3d);
        y4 = (int)(yD+Math.sin(Math.toRadians(rotation-270))*3d);

        r.fillPolygon(new int[]{x1, x2, x3, x4}, new int[]{y1, y2, y3, y4}, Color.orange);

//        x1 = (int)(xD+Math.cos(Math.toRadians(rotation))*2d);
//        y1 = (int)(yD+Math.sin(Math.toRadians(rotation))*2d);
//        x2 = (int)(xD+Math.cos(Math.toRadians(rotation-180))*2d);
//        y2 = (int)(yD+Math.sin(Math.toRadians(rotation-180))*2d);
//
//        r.fillPolygon(new int[]{x1, x2, x2, x1}, new int[]{y1+2, y2+2, y2+6, y1+6}, Color.orange);

        /*int x1 = (int)(xD+Math.cos(Math.toRadians(rotation))*4d);
        int y1 = (int)(yD+Math.sin(Math.toRadians(rotation))*4d);
        int x2 = (int)(xD+Math.cos(Math.toRadians(rotation-180))*4d);
        int y2 = (int)(yD+Math.sin(Math.toRadians(rotation-180))*4d);

        r.fillPolygon(new int[]{x1, x2, x2, x1}, new int[]{y1, y2, y2+8, y1+8}, Color.yellow);

        x1 = (int)(xD+Math.cos(Math.toRadians(rotation))*2d);
        y1 = (int)(yD+Math.sin(Math.toRadians(rotation))*2d);
        x2 = (int)(xD+Math.cos(Math.toRadians(rotation-180))*2d);
        y2 = (int)(yD+Math.sin(Math.toRadians(rotation-180))*2d);

        r.fillPolygon(new int[]{x1, x2, x2, x1}, new int[]{y1+2, y2+2, y2+6, y1+6}, Color.orange);*/
    }
}
