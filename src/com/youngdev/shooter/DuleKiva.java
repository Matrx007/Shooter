package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

public class DuleKiva extends WorldObject {
    private int step;
    private double angle;
    private double targetAngle;
    private double[] flares;
    private int flareStep;
    private double stroke;
    private boolean unstucked;
    private double waveStep;
    private double shrinkingMultiplier;
    public static double SpawnSpeed = 120d;

    public DuleKiva(int x, int y) {
        super(19, 14.5, 19);
        this.x = x;
        this.y = y;
        this.mask = new Mask.Rectangle(x-35, y-35, 70,70);
        flares = new double[5];
        flareStep = random.nextInt(240);
        this.depth = 19*1024+random.nextInt(1024);
        shrinkingMultiplier = 0.05d;

        this.aabbComponent = new AABBComponent(mask);
        new AABBCollisionManager(this, Main.collisionMap).unstuck();
        this.aabbComponent = null;

        unstucked = false;
    }

    private void createProjectTile(double direction) {
        double x = this.x + Math.cos(Math.toRadians(direction))*16d;
        double y = this.y + Math.sin(Math.toRadians(direction))*16d;
        Main.main.addEntity(new ProjectTile(
                (int) Math.round(x), (int)Math.round(y),
                direction));
    }

    private void createWave() {
        int patternSize = random.nextInt(3)+3;
        int sectors = random.nextInt(5)+5;
        boolean[] pattern = new boolean[patternSize];
        pattern[0] = true;
        for (int i = patternSize-1; i > 0; i--) {
            pattern[i] = random.nextInt(patternSize*2) == 0;
        }

        double sectorAngleWidth = 360d / sectors;
        double patternAngleWidth = sectorAngleWidth / patternSize;
        double randomAngleOffset = random.nextInt(359);

        for (int i = 0; i < patternSize*sectors; i++) {
            int patternIndex = i % patternSize;
            if(!pattern[patternIndex]) continue;
            int sector = i / patternSize;

            createProjectTile(randomAngleOffset + sector *
                    sectorAngleWidth + patternIndex * patternAngleWidth +
                    random.nextInt(360));
        }
    }

    @Override
    public void update(Input input) {
        shrinkingMultiplier *= 1+(0.05 * SpeedController.calcSpeed());
        shrinkingMultiplier = Math.min(shrinkingMultiplier, 1);

        if(!unstucked) {
            this.aabbComponent = new AABBComponent(mask);
            new AABBCollisionManager(this, Main.collisionMap).unstuck();
            this.aabbComponent = null;
            unstucked = true;
        }

        Player player = Main.main.player;
        if(Fly.distance(player.x, player.y, x, y) < 32 && !dead &&
                shrinkingMultiplier > 0.75d) {
            super.dead = true;
            Main.main.soundManager.playSound("open");
            Main.main.camera.shake(5f);
            for(int i = random.nextInt(15)+15; i >= 0; i--) {
                Main.main.addEntity(new Coin((int)x, (int)y,
                        random.nextInt(359)));
            }
            Main.main.camera.bluishEffect = 0.75f;
        } else if(Fly.distance(player.x, player.y, x, y) < 400 &&
                Fly.distance(player.x, player.y, x, y) > 60 &&
                shrinkingMultiplier > 0.9d) {
            double multiplier = Fly.distance(player.x,
                    player.y, x, y)/100d;
            waveStep+= Main.toSlowMotion(1d);
            if(waveStep > SpawnSpeed *multiplier) {
                waveStep = 0;
                createWave();
                shrinkingMultiplier = 0.5d;
            }
        }

        step+=1;

        int prevAngle = (int)angle;
        angle += Main.toSlowMotion((targetAngle-angle)*0.0125d);
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
        for (int i = flares.length - 1; i >= 0; i--) {
            double size = (2 + i * 3) * shrinkingMultiplier;
            double a = flares[i]; // Flare's angle
            double addX = Math.cos(Math.toRadians(size*8+a*4))*4;
            double addY = Math.cos(Math.toRadians(size*8+a*4))*4;
            points = new double[][]{
                    Fly.rotatePoint(x - size - addX, y - size - addY, x, y, -a*2 - 90d),
                    Fly.rotatePoint(x + size + addX, y - size - addY, x, y, -a*2 - 90d),
                    Fly.rotatePoint(x + size + addX, y + size + addY, x, y, -a*2 - 90d),
                    Fly.rotatePoint(x - size - addX, y + size - addY, x, y, -a*2 - 90d)
            };
            Fly.fillPoly(points, new Color(128 - i * 24, 64, 32), r);
        }
    }
}
