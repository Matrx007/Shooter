package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class DuleKiva extends WorldObject {
    private int step;
    private double angle;
    private double targetAngle;
    private double[] flares;
    private int flareStep;
    private double stroke;

    public DuleKiva(int x, int y) {
        super(12, 19, 14);
        this.x = x;
        this.y = y;
        this.mask = new Mask.Rectangle(x-35, y-35, 70,70);
        flares = new double[5];
        flareStep = random.nextInt(240);
        this.depth = 19*1024+random.nextInt(1024);
    }

    @Override
    public void update(Input input) {
        Player player = Main.main.player;
        if(Fly.distance(player.x, player.y, x, y) < 32 && !dead) {
            super.dead = true;
            Main.main.soundManager.playSound("explosion", 1, -20f);
            Main.main.camera.shake(5f);
            for(int i = random.nextInt(5)+5; i >= 0; i--) {
                Main.main.addEntity(new Coin((int)x, (int)y,
                        random.nextInt(359)));
            }
        }

        step+=1;

        int prevAngle = (int)angle;
        angle += (targetAngle-angle)*0.05;
        if(Math.abs(angle-targetAngle) < 1) {
            targetAngle = angle+random.nextInt(720)-360;
        }
        stroke = Math.min(Math.abs(8- Math.abs(prevAngle - angle)/2d), 8d);

        flareStep++;
        if(flareStep > 4) {
            double[] newFlares = new double[flares.length];
            flares[flares.length - 1] = angle;
            for (int i = 0; i < flares.length - 1; i++) {
                newFlares[i] = flares[i + 1];
            }
            newFlares[flares.length-1] = angle;
            flares = newFlares;
            flareStep = 0;
        }

//        if(flareStep > 9) {
//            flares[flares.length - 1] = angle;
//            flareStep = 0;
//        }
    }

    @Override
    public void render(Renderer r) {
        double[][] points;
        for (int i = flares.length-1; i >= 0; i--) {
            double size = 16+i*3;
            double a = flares[i]; // Flare's angle
            points = new double[][]{
                    Fly.rotatePoint(x - size, y - size, x, y, -a - 90),
                    Fly.rotatePoint(x + size, y - size, x, y, -a - 90),
                    Fly.rotatePoint(x + size, y + size, x, y, -a - 90),
                    Fly.rotatePoint(x - size, y + size, x, y, -a - 90)
            };
            Fly.fillPoly(points, new Color(32, 32, 80, 32), r);
        }

        points = new double[][]{
                Fly.rotatePoint(x-8, y-8, x, y, -angle-90),
                Fly.rotatePoint(x+8, y-8, x, y, -angle-90),
                Fly.rotatePoint(x+8, y+8, x, y, -angle-90),
                Fly.rotatePoint(x-8, y+8, x, y, -angle-90)
        };
        Fly.fillPoly(points, new Color(136, 16, 16), r);
        Stroke oldStroke = ((Graphics2D)r.getG()).getStroke();
        ((Graphics2D)r.getG()).setStroke(new BasicStroke((float) stroke));
        Fly.drawPoly(points, new Color(16, 16, 48), r);
        ((Graphics2D)r.getG()).setStroke(oldStroke);
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
