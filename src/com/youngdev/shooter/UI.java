package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

public class UI {
    // ### VARIABLES ###
    private int screenWidth;
    private int screenHeight;

    // ### SCORE ###
    public double scoreX, scoreY;
    private static final Alignment alignCenter =
            new Alignment(Alignment.HOR_CENTER, Alignment.VER_MIDDLE);
    private static final Font font =
            new Font("Romantiques", Font.PLAIN, 24);
    private double addY;
    private long prevMoney;
    private double healthOffsetX;
    private double healthOffsetY;

    // ### HEALTH ###
    private double healthAlpha;
    private double healthAlphaMultiplier;
    private int prevHealth;

    // ### GAME OVER ###
    boolean gameOverScreen;
    private Button restart;

    public UI() {
        screenWidth = Main.main.getE().width;
        screenHeight = Main.main.getE().height;
        restart = new Button(Main.main.getE().getHeight()/2, "Proovi uuesti");
    }

    public void update(Input i) {
        Player player = Main.main.player;
        double camX = Main.main.camera.cX;
        double camY = Main.main.camera.cY;
        double playerX = player.x - camX;
        double playerY = player.y - camY;

        // ### SCORE ###
        if(player.money != prevMoney)
            addY = 8d;
        addY *= 0.9;

        scoreX = (screenWidth / 2d - playerX)*(-0.25d-(addY/24d)) + screenWidth/2d;
        scoreY = (screenHeight/ 2d - playerY)*-0.75d +
                screenHeight/5d + addY;

        prevMoney = player.money;

        // ### HEALTH ###
        if(player.health != prevHealth)
            healthAlpha = 1d;
        healthAlpha *= 0.99d;

        if(player.health <= 0) {
            gameOverScreen = true;
        }

        healthOffsetX = (screenWidth/ 2d - playerX)*(-1.5d) +
                screenWidth/2d;
        healthOffsetY = (screenHeight/ 2d - playerY)*-1.5d +
                screenHeight/1.75 + addY;

        healthAlphaMultiplier = Math.max(0.1d, Math.min(1d
                - Math.abs((healthOffsetX - screenWidth / 2d) / 32d)
                - Math.abs((healthOffsetY-screenHeight/2d)/32d), 0.5d));

        prevHealth = player.health;

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
        }
    }

    public void render(Renderer r) {
        Graphics2D g2d = (Graphics2D) r.getG();
        Player player = Main.main.player;

        // ### SCORE ###
        r.setFont(font);
        r.drawText(player.money+"", scoreX, scoreY, 36+ (int)Math.round(addY),
                alignCenter, new Color(60, 60, 150));

        // ### HEALTH ###
        int healthMax = player.health;
        double spacingAngle = 30;
        for(int i = player.health-1; i >= 0; i--) {
            double angle = Math.toRadians(i*spacingAngle - (healthMax*spacingAngle-spacingAngle)/2d + 90D);
            double xx = Math.cos(angle) * 80d + healthOffsetX;
            double yy = Math.sin(angle) * 80d + healthOffsetY;
            int alpha = (int) Math.round(healthAlpha * 255d * healthAlphaMultiplier);

            Stroke stroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(4f));
            r.drawCircle(xx, yy, 24, new Color(140, 16, 16, alpha));
            g2d.setStroke(new BasicStroke(2f));
            r.drawCircle(xx, yy, 24, new Color(90, 16, 16, alpha));
            g2d.setStroke(stroke);
        }

        if(gameOverScreen) {
            // ### GAME OVER SCREEN ###
            r.drawText("GAME OVER", screenWidth / 2d,
                    80d, 32, alignCenter, Color.black);
            restart.render(r);
        }
    }
}
