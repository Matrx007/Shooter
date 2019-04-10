package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.SplittableRandom;

public class UI {
    static final Color UI_BACK = new Color(151, 144, 55, 192);
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
    private long prevScore;
    private double healthOffsetX;
    private double healthOffsetY;
    private double scoreOffsetX;
    private double scoreOffsetTargetX;
    private long storedScore;

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
    private Button restartButton;
    private Button mainMenuButton;
    private boolean prevGameOver;
    private SplittableRandom random;

    // ### CUSTOM GAME SETTINGS ###
    public boolean customize;
    private ValueScroller[] scrollers;
    private Button play;
    float speedMultiplier;
    private Button gtMainMenu;

    // ### PAUSE MENU ###
    public boolean gamePaused;
    private Button[] pauseButtons;

    // ### BACKGROUND ###
    public double backTargetX;
    double backX;
    public double hoverTargetY;
    private double hoverY;
    public double hoverAddTargetX;
    private double hoverAddX;
    boolean hovering;

    // ### MAIN MENU ###
    public boolean mainMenu;
    private Button[] mainMenuOptions;
    private BufferedImage gameLogo;

    public UI() {
        customize = false;
        screenWidth = Main.main.getE().width;
        screenHeight = Main.main.getE().height;
        restartButton = new Button(Main.main.getE().getHeight()/2,
                "Proovi uuesti");
        mainMenuButton = new Button(Main.main.getE().getHeight()/2+32,
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

        backTargetX = 0d;
        backX = 0d;

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
                        "Mängu kiirus on $x",
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

        // ### MAIN MENU ###
        mainMenu = true;
        mainMenuOptions = new Button[]{
                new Button(64, "Klassikaline"),
                new Button(96, "Muuda raskusastet"),
                new Button(128, "Lahku mängust")
        };

        BufferedImage loadedLogo = new com.engine.libs.rendering.Image(
                "/com/youngdev/shooter/res/dulekiva.png").getImage();
        double newHeight = 64;
        double newWidth = (double) loadedLogo.getWidth() /
                (double) loadedLogo.getHeight() * newHeight;
        java.awt.Image logo = loadedLogo.getScaledInstance((int) newWidth,
                (int) newHeight, java.awt.Image.SCALE_FAST);
        gameLogo = new BufferedImage((int) newWidth, (int) newHeight,
                BufferedImage.TYPE_INT_ARGB);
        gameLogo.getGraphics().drawImage(logo, 0, 0, null);

        speedMultiplier = 1f;
    }

    public void update(Input i) {
        Player player = Main.main.player;
        double camX = Main.main.camera.cX;
        double camY = Main.main.camera.cY;
        double playerX = player.x - camX;
        double playerY = player.y - camY;

        // ### BACK ###
        hovering = false;

        // ### SCORE ###
        if(player.score != prevScore && !gameOverScreen)
            addY = 16d;
        addY *= 0.9;

        scoreX = (screenWidth / 2d - playerX)*(-0.25d-(addY/24d)) + screenWidth/2d;
        scoreY = (screenHeight/ 2d - playerY)*-0.75d +
                screenHeight/5d + addY;

        prevScore = player.score;

        if(!gameOverScreen) storedScore = prevScore;

        scoreOffsetX += (scoreOffsetTargetX - scoreOffsetX) * 0.125d;

        // ### HEALTH ###
        if(player.health != prevHealth && !Main.notInGame &&
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

        backTargetX = 0;

        // ### GAME OVER ###
        boolean ignoreMain = false;
        if(gameOverScreen) {
            backTargetX = 192;
            Main.main.camera.updateValues = false;
            healthAlpha = Math.max(0, healthAlpha-0.05);

            // Effects
            Main.main.camera.blackAndWhiteEffect =
                    (float) StrictMath.min(
                    Main.main.camera.blackAndWhiteEffect
                            + 0.01d, 1d);
            Main.main.camera.bitCrushEffect  =
                    (float) StrictMath.min(
                    Main.main.camera.bitCrushEffect
                            + 0.01d, 0.125d);
            // ------

            Main.main.player.isDead = true;
            SpeedController.setSpeed(0.25f);


            // Buttons
            restartButton.update(i);
            if(restartButton.pressed) {
                Main.main.restart();
                Main.main.camera.bitCrushEffect = 0.5f;
                gameOverScreen = false;
                SpeedController.resetMultipliers();
                SpeedController.setSpeed(1f);
            }
            mainMenuButton.update(i);
            if(mainMenuButton.pressed) {
                System.out.println("true");
                gamePaused = false;
                Main.main.restart();
                Main.notInGame = true;
                mainMenu = true;
                gameOverScreen = false;
                ignoreMain = true;
            }

            scoreOffsetTargetX = -screenWidth/4d;
        } else {
            scoreOffsetTargetX = 0;
            Main.main.camera.updateValues = true;
            healthAlpha = 1f;
        }

        // ### SETTINGS ###
        if(customize) {
            backTargetX = 300;
            gtMainMenu.update(i);

            if(gtMainMenu.pressed) {
                customize = false;
                mainMenu = true;
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
                customize = false;
                Main.notInGame = false;
                Main.main.restart();
                speedMultiplier = (float)scrollers[4].value;
                mainMenu = false;
                Main.notInGame = false;
            }
        }

        SpeedController.multiply(speedMultiplier);

        // ### PAUSE MENU ###
        if(i.isKeyDown(KeyEvent.VK_ESCAPE)) {
            if(!customize && !Main.notInGame) {
                if(gamePaused) {
                    gamePaused = false;
                    SpeedController.resetMultipliers();
                    Main.main.camera.blackAndWhiteEffect = 0f;
                } else {
                    gamePaused = true;
                }
            }
        }
        if(gamePaused && !ignoreMain) {
            scoreOffsetTargetX = screenWidth/4d;
            backTargetX = 192;
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
                Main.notInGame = true;
                mainMenu = true;
            }

            if(pauseButtons[2].pressed) {
                System.exit(0);
            }
        }

        // ### MAIN MENU ###
        if(mainMenu && !ignoreMain) {
            backTargetX = 192;
            for (Button button : mainMenuOptions) {
                button.update(i);
            }

            if(mainMenuOptions[0].pressed) {
                DuleKiva.SpawnSpeed = 120d;
                Player.MaxSpeed = 3f;
                Player.MaxHealth = 5;
                Main.DuleKivaHaruldus = 10;
                speedMultiplier = 1f;
                SpeedController.resetMultipliers();
                mainMenu = false;
                Main.notInGame = false;
            }

            if(mainMenuOptions[1].pressed) {
                customize = true;
                mainMenu = false;
            }

            if(mainMenuOptions[2].pressed) {
                System.exit(0);
            }
        }

        // ### BACKGROUND ###
        backX += (backTargetX - backX) * 0.1d;

        hoverAddTargetX = hovering ? 1 : 0;
        hoverAddX += (hoverAddTargetX - hoverAddX) * 0.125d;
        hoverY += (hoverTargetY - hoverY) * 0.25d;
    }

    public void render(Renderer r) {
        Graphics2D g2d = (Graphics2D) r.getG();
        Player player = Main.main.player;

        r.fillRectangle(0, 0,
                (int)Math.round(backX),
                screenHeight, UI_BACK);

        r.fillRectangle(0, (int)Math.round(hoverY),
                (int)Math.round(backX),
                24, new Color(
                        255, 255, 255,
                        (int) Math.round(hoverAddX * 32d)));

        if(!Main.notInGame) {
            // ### SCORE ###
            r.setFont(font1);
            r.drawText(storedScore + "", scoreX+
                            Math.round(scoreOffsetX), scoreY,
                    36 + (int) Math.round(addY),
                    alignCenter, new Color(16, 16, 16));

            // ### HEALTH ###
            double multiply = 1d + healthAliveFast;
            int offsetX = (int) (screenWidth / 2d + healthOffsetX);
            int offsetY = (int) (screenHeight / 4d * 3d + healthOffsetY);
            int[] xPoints = new int[heartPoints.length];
            int[] yPoints = new int[heartPoints.length];
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
                r.drawText("KAOTUS", screenWidth * 0.75d,
                        80d, 28, alignCenter, Color.black);
                restartButton.render(r);
                mainMenuButton.render(r);
            }
        }
        
        // ### SETTINGS ###
        if(customize){
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

        // ### MAIN MENU ###
        if(mainMenu) {
            for(Button button : mainMenuOptions) {
                button.render(r);
            }

            r.drawImage((screenWidth/2d-gameLogo.
                                    getWidth())/2d, 0,
                    gameLogo);
        }
    }
}
