package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
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

    // ### CUSTOM GAME SETTINGS ###
    public boolean settings;
    private ValueScroller[] scrollers;
    private Button play;
    float speedMultiplier;
    private Button gtMainMenu;

    // ### PAUSE MENU ###
    public boolean gamePaused;
    private Button[] pauseButtons;

    public UI() {
        settings = false;
        screenWidth = Main.main.getE().width;
        screenHeight = Main.main.getE().height;
        restart = new Button(Main.main.getE().getHeight()/2,
                "Proovi uuesti");
        mainMenu = new Button(Main.main.getE().getHeight()/2+32,
                "Põhimenüü");
        gtMainMenu = new Button(8, "Põhimenüü");

        // Heart symbol
        random = new SplittableRandom();
        heartPointOffsets = new Vec3d[heartPoints.length];
        for (int i = 0; i < heartPointOffsets.length; i++) {
            heartPointOffsets[i] = new Vec3d(
                    random.nextDouble()*360d, 0, 0);
        }

        prevHealth = Main.main.player.health;
        healthAlive = 0d;
        healthAlpha = 1d;

        int startY = Main.main.getE().getHeight()/5;
        int add = 24+2;
        DecimalFormat df = new DecimalFormat("#.##");
        scrollers = new ValueScroller[]{
                new ValueScroller(startY, "Mängija kiirus on $ px/s",
                        1d, 4d, 0.5d,
                        3d) {
                    @Override
                    public String getUIValue() {
                        return df.format(value*60);
                    }
                },
                new ValueScroller(startY+add, "DuleKiva'de haruldus on $%",
                        0d, 20d, 1d,
                        10d) {
                    @Override
                    public String getUIValue() {
                        return df.format(100-value*5);
                    }
                },
                new ValueScroller(startY+add*2,
                        "DuleKiva'de ründamise kiirus on $s",
                        20d, 240d, 10d,
                        120d) {
                    @Override
                    public String getUIValue() {
                        return df.format(value/60d);
                    }
                },
                new ValueScroller(startY+add*3,
                        "Elusi on $",
                        1d, 20d, 1d,
                        5d) {
                    @Override
                    public String getUIValue() {
                        return (int)value+"";
                    }
                },
                new ValueScroller(startY+add*4,
                        "Mängukiirus on $x",
                        0.5d, 1.5d, 0.1d,
                        1d) {
                    @Override
                    public String getUIValue() {
                        return df.format(value);
                    }
                }
        };
        play = new Button(Main.main.getE().height-32, "Alusta");

        int bottom = Main.main.getE().height;
        pauseButtons = new Button[]{
                new Button(8, "Jätka mängu"),
                new Button(bottom-64, "Avamenüü"),
                new Button(bottom-32, "Lahku mängust")
        };

        speedMultiplier = 1f;
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
        if(player.health != prevHealth && !Main.startMenuMode &&
                !gameOverScreen) {
            healthAlive = 1d;
            healthAliveFast = 1d;
            Main.main.soundManager.playSound("gameOver",
                    -2.5f);
            Main.main.camera.bluishEffect = 0.5f;
        }
        healthAliveFast *= 0.90d;
        healthAlive     *= 0.99d;

        prevGameOver = gameOverScreen;
        if(player.health <= 0) {
            gameOverScreen = true;
            if(!prevGameOver)
                Main.main.soundManager.playSound("gameOver",
                        0f);
        }

        healthOffsetX = (screenWidth/ 2d - playerX)*-0.75d+screenHeight/10d*7d;
        healthOffsetY = (screenHeight/ 2d - playerY)*-0.75d+screenHeight/10d;

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
            healthAlpha = Math.max(0, healthAlpha-0.05);
            Main.main.camera.blackAndWhiteEffect =
                    (float) StrictMath.min(
                    Main.main.camera.blackAndWhiteEffect
                            + 0.01d, 1d);
            Main.main.player.isDead = true;
            SpeedController.setSpeed(0.25f);
            restart.update(i);
            if(restart.pressed) {
                Main.main.restart();
                Main.main.camera.bitCrushEffect = 0.5f;
                gameOverScreen = false;
                SpeedController.resetMultipliers();
                SpeedController.setSpeed(1f);
            }
            mainMenu.update(i);
            if(mainMenu.pressed) {
                Main.main.restart();
                Main.startMenuMode = true;
                gameOverScreen = false;
            }
        } else {
            healthAlpha = 1f;
        }

        // ### SETTINGS ###
        if(settings) {
            gtMainMenu.update(i);

            if(gtMainMenu.pressed) {
                settings = false;
            }

            for (int j = 0; j < scrollers.length; j++) {
                scrollers[j].update(i);
            }

            play.update(i);
            if(play.pressed) {
                Player.MaxSpeed = (float)scrollers[0].value;
                Main.DuleKivaHaruldus = (int)scrollers[1].value;
                DuleKiva.SpawnSpeed = scrollers[2].value;
                Player.MaxHealth = (int)scrollers[3].value;
                settings = false;
                Main.startMenuMode = false;
                Main.main.restart();
                speedMultiplier = (float)scrollers[4].value;
            }
        }

        SpeedController.multiply(speedMultiplier);

        // ### PAUSE MENU ###
        if(i.isKeyDown(KeyEvent.VK_ESCAPE)) {
            if(!settings && !Main.startMenuMode) {
                if(gamePaused) {
                    gamePaused = false;
                    SpeedController.resetMultipliers();
                    Main.main.camera.blackAndWhiteEffect = 0f;
                } else {
                    gamePaused = true;
                }
            }
        }
        if(gamePaused) {
            SpeedController.multiply(0f);
            Main.main.camera.blackAndWhiteEffect = Math.min(Main.main.camera.
                    blackAndWhiteEffect+0.05f, 1f);

            for (Button button : pauseButtons) button.update(i);

            if(pauseButtons[0].pressed) {
                gamePaused = false;
                SpeedController.resetMultipliers();
                Main.main.camera.blackAndWhiteEffect = 0f;
            }

            if(pauseButtons[1].pressed) {
                gamePaused = false;
                Main.main.restart();
                Main.startMenuMode = true;
            }

            if(pauseButtons[2].pressed) {
                System.exit(0);
            }
        }
    }

    public void render(Renderer r) {
        Graphics2D g2d = (Graphics2D) r.getG();
        Player player = Main.main.player;

        if(!Main.startMenuMode) {
            // ### SCORE ###
            r.setFont(font1);
            r.drawText(storedMoney + "", scoreX, scoreY, 36 + (int) Math.round(addY),
                    alignCenter, new Color(26, 29, 0));

            // ### HEALTH ###
            double multiply = 1d + healthAliveFast;
            int offsetX = (int) (screenWidth / 2d + healthOffsetX);
            int offsetY = (int) (screenHeight / 4d * 3d + healthOffsetY);
            int xPoints[] = new int[heartPoints.length];
            int yPoints[] = new int[heartPoints.length];
            for (int i = 0; i < heartPoints.length; i++) {
                xPoints[i] = offsetX + (int) (heartPoints[i].x * multiply + heartPointOffsets[i].y * 4d);
                yPoints[i] = offsetY + (int) (heartPoints[i].y * multiply + heartPointOffsets[i].z * 4d);
            }
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(3f));
            r.drawPolygon(xPoints, yPoints, heartPoints.length, new Color(145, 32, 20, (int) (healthAlpha * 255d)));
            r.setFont(font2);
            r.drawText(Math.max(player.health, 0) + "", screenWidth / 2d + healthOffsetX,
                    screenHeight / 4d * 3d + healthOffsetY, 30 + (int) (healthAliveFast * 10d), alignCenter,
                    new Color(135, 22, 10, (int) (healthAlpha * 255d)));
            g2d.setStroke(oldStroke);

            // ### GAME OVER SCREEN ###
            if (gameOverScreen) {
                r.setFont(font1);
                r.drawText("GAME OVER", screenWidth / 2d,
                        80d, 32, alignCenter, Color.black);
                restart.render(r);
                mainMenu.render(r);
            }
        }
        
        // ### SETTINGS ###
        if(settings){
            gtMainMenu.render(r);
            for (int i = 0; i < scrollers.length; i++) {
                scrollers[i].render(r);
            }
            play.render(r);
        }

        // ### PAUSE MENU ###
        if(gamePaused) {
            for (Button button : pauseButtons) button.render(r);
        }
    }
}
