package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import java.awt.*;
import java.util.SplittableRandom;

public class UI {
    // ### VARIABLES ###
    private int screenWidth;
    private int screenHeight;

    // ### SCORE ###
    public double scoreX, scoreY;
    private static final Alignment alignCenter =
            new Alignment(Alignment.HOR_CENTER, Alignment.VER_MIDDLE);
    private static final Font font1 =
            new Font("Romantiques", Font.PLAIN, 24);
    private static final Font font2 =
            new Font("Shanghai", Font.PLAIN, 30);
    private double addY;
    private long prevMoney;
    private double healthOffsetX;
    private double healthOffsetY;
    private long storedMoney;

    // ### HEALTH ###
    private static final Vec2d[] heartPoints = new Vec2d[]{
            new Vec2d(0, -18), // Top

            new Vec2d(6, -24),
            new Vec2d(18, -26),
            new Vec2d(28, -17),
            new Vec2d(24, 2),
            new Vec2d(12, 18),

            new Vec2d(0, 26), // Bottom

            new Vec2d(-12, 18),
            new Vec2d(-24, 2),
            new Vec2d(-28, -17),
            new Vec2d(-18, -26),
            new Vec2d(-6, -24),
    };
    private Vec3d[] heartPointOffsets;
    private double healthAlpha;
    private double healthAlive;
    private double healthAliveFast;
    private double healthAlphaMultiplier;
    private int prevHealth;

    // ### GAME OVER ###
    private boolean gameOverScreen;
    private Button restart;
    private Button mainMenu;
    private boolean prevGameOver;
    private SplittableRandom random;

    public UI() {
        screenWidth = Main.main.getE().width;
        screenHeight = Main.main.getE().height;
        restart = new Button(Main.main.getE().getHeight()/2,
                "Proovi uuesti");
        mainMenu = new Button(Main.main.getE().getHeight()/2+32,
                "Põhimenüü");

        // Heart symbol
        random = new SplittableRandom();
        heartPointOffsets = new Vec3d[heartPoints.length];
        for (int i = 0; i < heartPointOffsets.length; i++) {
            heartPointOffsets[i] = new Vec3d(
                    random.nextDouble()*360d, 0, 0);
        }

        prevHealth = Main.main.player.health;
        healthAlive = 0d;
    }

    public void update(Input i) {
        Player player = Main.main.player;
        double camX = Main.main.camera.cX;
        double camY = Main.main.camera.cY;
        double playerX = player.x - camX;
        double playerY = player.y - camY;

        // ### SCORE ###
        if(player.money != prevMoney && !gameOverScreen)
            addY = 16d;
        addY *= 0.9;

        scoreX = (screenWidth / 2d - playerX)*(-0.25d-(addY/24d)) + screenWidth/2d;
        scoreY = (screenHeight/ 2d - playerY)*-0.75d +
                screenHeight/5d + addY;

        prevMoney = player.money;

        if(!gameOverScreen) storedMoney = prevMoney;

        // ### HEALTH ###
        if(player.health != prevHealth) {
            healthAlpha = 1d;
            healthAlive = 1d;
            healthAliveFast = 1d;
            Main.main.camera.bluishEffect = 0.5f;
        }
        healthAliveFast *= 0.9d;
        healthAlive     *= 0.99d;
        healthAlpha     *= 0.99d;

        prevGameOver = gameOverScreen;
        if(player.health <= 0) {
            gameOverScreen = true;
            if(!prevGameOver)
                Main.main.soundManager.playSound("gameOver",
                        -5f);
        }

        healthOffsetX = (screenWidth/ 2d - playerX)*(-1.5d);
        healthOffsetY = (screenHeight/ 2d - playerY)*-1.5d+screenHeight/10d;

        healthAlphaMultiplier = Math.max(0.1d, Math.min(1d
                - Math.abs((healthOffsetX - screenWidth / 2d) / 32d)
                - Math.abs((healthOffsetY-screenHeight/2d)/32d), 0.5d));

        prevHealth = player.health;

        for (int j = 0; j < heartPointOffsets.length; j++) {
            heartPointOffsets[j].x += random.nextInt(24);
            heartPointOffsets[j].y = Math.cos(Math.toRadians(
                    heartPointOffsets[j].x))*healthAlive;
            heartPointOffsets[j].z = Math.sin(Math.toRadians(
                    heartPointOffsets[j].x))*healthAlive;
        }

        // ### GAME OVER ###
        if(gameOverScreen) {
            Main.main.camera.blackAndWhiteEffect =
                    (float) StrictMath.min(
                    Main.main.camera.blackAndWhiteEffect
                            + 0.01d, 1d);
            Main.main.player.isDead = true;
            Main.slowMotionSpeed = 0.25f;
            restart.update(i);
            if(restart.pressed) {
                Main.main.restart();
                Main.main.camera.bitCrushEffect = 0.5f;
                gameOverScreen = false;
            }
            mainMenu.update(i);
            if(mainMenu.pressed) {
                Main.main.restart();
                Main.startMenuMode = true;
                gameOverScreen = false;
            }
        }
    }

    public void render(Renderer r) {
        Graphics2D g2d = (Graphics2D) r.getG();
        Player player = Main.main.player;

        // ### SCORE ###
        r.setFont(font1);
        r.drawText(storedMoney+"", scoreX, scoreY, 36+ (int)Math.round(addY),
                alignCenter, new Color(26, 29, 0));

        // ### HEALTH ###
        double multiply = 1.25d + healthAliveFast;
        int offsetX = (int)(screenWidth/2d + healthOffsetX);
        int offsetY = (int)(screenHeight/4d*3d + healthOffsetY);
        int xPoints[] = new int[heartPoints.length];
        int yPoints[] = new int[heartPoints.length];
        for (int i = 0; i < heartPoints.length; i++) {
            xPoints[i] = offsetX+(int)(heartPoints[i].x*multiply+heartPointOffsets[i].y*4d);
            yPoints[i] = offsetY+(int)(heartPoints[i].y*multiply+heartPointOffsets[i].z*4d);
        }
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(3f));
        r.drawPolygon(xPoints, yPoints, heartPoints.length, new Color(145, 32, 20, (int)(1*255d)));
        r.setFont(font2);
        r.drawText(Math.max(player.health, 0)+"", screenWidth/2d + healthOffsetX,
                screenHeight/4d*3d + healthOffsetY, 40+(int)(healthAliveFast*15d), alignCenter,
                new Color(135, 22, 10, (int)(1*255d)));
        g2d.setStroke(oldStroke);

        if(gameOverScreen) {
            // ### GAME OVER SCREEN ###
            r.setFont(font1);
            r.drawText("GAME OVER", screenWidth / 2d,
                    80d, 32, alignCenter, Color.black);
            restart.render(r);
            mainMenu.render(r);
        }
    }
}
