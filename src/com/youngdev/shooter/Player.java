package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.game.GameObject;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Player extends Healable {

    public ArrayList<UniParticle> particles;
    private Random random;
    private AABBCollisionManager cm;
    boolean blinkingON;
    private boolean found, waitingForRelease;
    public boolean buildingMode, inventoryOpen, leftHandShooting, rightHandShooting, clipOverlayOpen;
    public int xx, yy, invSize = 24, midX, midY, selectedItem, time=60, timer, leftHandReload, rightHandReload,
            reloadTime = 50, bulletTimingCap = 5, leftHandBulletTimingCapCounter, leftHandBulletAmountCounter,
            rightHandBulletTimingCapCounter, rightHandBulletAmountCounter, bulletsPerShot = 5, ammo = 34, maxAmmo = 45,
            clip = 10, money;
    public double lastCoinX, lastCoinY, coinOverlayAlpha, coinOverlayX, coinOverlayY, clipOverlayAlpha,
            clipOverlayRotation, clipOverlayRotationSpeed, clipOverlayRotationTarget, health, healthMax,
            hunger, hungerMax, statsOverlayAlpha, statsOverlayRotation, statsOverlayRotationSpeed,
            statsOverlayRotationTarget, autoReloadBlinkingTime, autoReloadBlinkingTimer, autoReloadTime,
            autoReloadTimer, autoReloadMaximumAmmo, autoReloadY, autoReloadTargetY;
    private int blinkingTime = 30;
    public int[] items;
    private String[] itemNames;
    private StructuralBlock[] itemSamples;
    private static Color baseColor = new Color(130, 32, 78); // 170, 32, 128
    private static int statsReloadTargetHeight = -56-12-16;
    private static int reloadTargetHeight = -16;
    private float speedX, targetSpeedX, speedY, targetSpeedY, maxSpeed, speedStep, blinkingTimer;

    public Player(int x, int y) {
        super(x, y, 4, 4, 200, 0, 10, false, false);
        this.depth = 10;
        this.random = new Random();
        particles = new ArrayList<>();

        // HERE: Fix depth
        depth = random.nextInt(1023)+depth*1024;

        leftHandReload = 0;
        rightHandReload = 0;
        leftHandShooting = false;
        rightHandShooting = false;
        leftHandBulletAmountCounter = 0;
        rightHandBulletAmountCounter = 0;
        leftHandBulletTimingCapCounter = 0;
        rightHandBulletTimingCapCounter = 0;

        lastCoinX = x;
        lastCoinY = y-256;

        health = 100d;
        hunger = 100d;
        healthMax = 150d;
        hungerMax = 150d;

        items = new int[5];
        itemNames = new String[] {
                "Empty Hand",
                "Rock Wall",
                "Wooden Wall",
                "Plant Fiber Wall",
                "Wood Flooring"
        };
        itemSamples = new StructuralBlock[] {
                null,
                new StructuralBlock(0, 0, StructuralBlock.TYPE_ROCKS),
                new StructuralBlock(0, 0, StructuralBlock.TYPE_WOOD),
                new StructuralBlock(0, 0, StructuralBlock.TYPE_FIBER),
                new StructuralBlock(0, 0, StructuralBlock.TYPE_WOOD_FLOORING)
        };

        inventoryOpen = false;

        midX = Main.main.getE().getWidth()/2;
        midY = Main.main.getE().getHeight()/2;

        core = Main.main.getE();

        this.buildingMode = true;

        super.x = x;
        super.y = y;

        this.xx = x;
        this.yy = y;

        this.maxSpeed = 3f;
        this.targetSpeedX = 0;
        this.targetSpeedY = 0;
        this.speedX = 0;
        this.speedY = 0;
        this.speedStep = 0.5f;
        this.blinkingTimer = 0;

        this.clipOverlayAlpha = 0d;
        this.clipOverlayOpen = false;
        this.clipOverlayRotation = 0;
        this.clipOverlayRotationSpeed = 0;
        this.clipOverlayRotationTarget = 0;

        this.statsOverlayAlpha = 0;
        this.statsOverlayRotation = 0;
        this.statsOverlayRotationSpeed = 0;
        this.statsOverlayRotationTarget = 0;
        this.waitingForRelease = false;

        autoReloadBlinkingTime = 15;
        autoReloadBlinkingTimer = 0;
        autoReloadTime = 120;
        autoReloadTimer = 0;
        autoReloadMaximumAmmo = 10;
        autoReloadY = reloadTargetHeight;
        autoReloadTargetY = reloadTargetHeight;

        cm = new AABBCollisionManager(this, Main.collisionMap);
    }

    @Override
    public void update(Input i) {
//        boolean prevInvOpen = inventoryOpen;
        blinkingTimer += 1d/Main.toSlowMotion(1d);

        if(blinkingTimer >= blinkingTime) {
            blinkingON = !blinkingON;
            blinkingTimer = 0;
        }

        // HERE: Use keyboard to move
        int moveX = (i.isKey(KeyEvent.VK_D) ? 1 : 0) - (i.isKey(KeyEvent.VK_A) ? 1 : 0);
        int moveY = (i.isKey(KeyEvent.VK_S) ? 1 : 0) - (i.isKey(KeyEvent.VK_W) ? 1 : 0);

        /*if(moveX != 0 || moveY != 0) {
            inventoryOpen = false;
            timer = time;
        }*/

        targetSpeedX = moveX * maxSpeed;
        targetSpeedY = moveY * maxSpeed;

        speedX += Math.signum(targetSpeedX - speedX)*speedStep;
        speedY += Math.signum(targetSpeedY - speedY)*speedStep;

        speedX = Math.max(-maxSpeed, Math.min(maxSpeed, speedX));
        speedY = Math.max(-maxSpeed, Math.min(maxSpeed, speedY));

//        System.out.println(Main.collisionMap.size());
        cm.move(Main.toSlowMotion(speedX), Main.toSlowMotion(speedY));

        this.xx = (int) x;
        this.yy = (int) y;

        for (int j = 0; j < 2; j++) {
            int tone = random.nextInt(30)-15;
            int sDir = random.nextInt(359);
            int sDistance = random.nextInt(8);
            int xx = this.xx + (int) (Math.cos(Math.toRadians(sDir)) * sDistance);
            int yy = this.yy + (int) (Math.sin(Math.toRadians(sDir)) * sDistance);
            int fadingSpeed = random.nextInt(8)+8;
            int size = random.nextInt(4)+2;
            Color color = new Color(baseColor.getRed()+tone, baseColor.getGreen()+tone, baseColor.getBlue()+tone);
            UniParticle.FadingProcess fadingProcess = new UniParticle.FadingProcess(255, fadingSpeed, false);
            particles.add(new UniParticle(xx, yy, size, true, color, fadingProcess));
        }

        particles.forEach(UniParticle::update);
        particles.removeIf(particle -> particle.dead);

        if(i.isButtonDown(1) && ammo > 0 && autoReloadTimer == 0) {
            ammo--;
//            System.out.println("Shot");
            int addX = (int)(Math.cos(Math.toRadians(Fly.angle(x, y, i.getRelativeMouseX(), i.getRelativeMouseY())-180))*10d);
            int addY = (int)(Math.sin(Math.toRadians(Fly.angle(x, y, i.getRelativeMouseX(), i.getRelativeMouseY())-180))*10d);

            int dir = (int) Fly.angle(xx+addX, yy+addY, i.getRelativeMouseX(), i.getRelativeMouseY());

            double knockBackX = Math.cos(Math.toRadians(dir)) * 4d;
            double knockBackY = Math.sin(Math.toRadians(dir)) * 4d;

            cm.move(knockBackX, knockBackY);

            Main.main.camera.cX -= knockBackX;
            Main.main.camera.cY -= knockBackY;

            Arrow arrow = new Arrow(xx+addX, yy+addY, dir-180);
            arrow.shotByFriendly = !isEnemy;
            arrow.addX = speedX;
            arrow.addY = speedY;
            Main.main.entities.add(arrow);
            Main.main.findOnScreenObjects();
            Main.main.camera.shake(0.5f);
//            Main.main.camera.bluishEffect = 1f;
        }

        boolean arrowNear = false;
        if (Main.main.entities.size() > 0) {
            for(GameObject entity : Main.main.entities) {
                if(entity instanceof Arrow) {
                    if(!((Arrow) entity).shotByFriendly)
                        if(Fly.distance(x, y, entity.x, entity.y) < 80) {
                            arrowNear = true;
                            break;
                        }
                }
            }
        }

        boolean enemyNear = false;
        if (Main.main.entities.size() > 0) {
            for(GameObject obj : Main.main.entities) {
                if(obj instanceof Healable) {
                    if(((Healable) obj).isEnemy)
                        if(Fly.distance(x, y, obj.x, obj.y) < 50) {
                            enemyNear = true;
                            break;
                        }
                }
            }
        }

        if(arrowNear || enemyNear || inventoryOpen) {
            Main.slowMotionSpeed -= 0.05f;
            Main.main.camera.bluishEffect += 0.05f;
        } else {
//            System.out.println("SlMoSp: "+Main.slowMotionSpeed);
//            System.out.println("BlFx: "+Main.main.camera.bluishEffect);
            Main.slowMotionSpeed += 0.05f;
            Main.main.camera.bluishEffect -= 0.05f;
        }

        Main.slowMotionSpeed = (float)AdvancedMath.setRange(Main.slowMotionSpeed, 0.4d, 1d);
        Main.main.camera.bluishEffect = (float)AdvancedMath.setRange(Main.main.camera.bluishEffect, 0d, 0.5d);

        // HERE: Clip overlay
        coinOverlayAlpha = Math.max(0, coinOverlayAlpha-Main.toSlowMotion(2));
        coinOverlayX += (lastCoinX - coinOverlayX) * 0.1d;
        coinOverlayY += (lastCoinY - coinOverlayY) * 0.1d;
        clipOverlayOpen = i.isKey(KeyEvent.VK_V) || i.isButton(3);
        if(clipOverlayOpen) {
            clipOverlayRotationTarget = 110;
            clipOverlayAlpha += 24;
        } else {
            clipOverlayRotationTarget = 1;
            clipOverlayAlpha -= 24;
        }
        clipOverlayRotation += (clipOverlayRotationTarget - clipOverlayRotation) * 0.1d;
        clipOverlayAlpha = AdvancedMath.setRange(clipOverlayAlpha, 0, 255);

        // HERE: Player stats overlay
        boolean statsOverlayOpen = i.isButton(3);
        if(statsOverlayOpen) {
            statsOverlayRotationTarget = 0;
            statsOverlayAlpha += 24;
            autoReloadTargetY = statsReloadTargetHeight;
        } else {
            autoReloadTargetY = reloadTargetHeight;
            statsOverlayRotationTarget = -90;
            statsOverlayAlpha -= 24;
        }
        statsOverlayRotation += (statsOverlayRotationTarget - statsOverlayRotation) * 0.1d;
        statsOverlayAlpha = AdvancedMath.setRange(statsOverlayAlpha, 0, 160);

        autoReloadY += (autoReloadTargetY - autoReloadY) * 0.1d;

        boolean rPressed = i.isKey(KeyEvent.VK_R);

        if(!rPressed) {
            waitingForRelease = false;

            if(statsOverlayOpen && ammo > autoReloadMaximumAmmo) {
                autoReloadBlinkingTimer = 0;
                autoReloadTimer = 0;
            }
        }

        if(clip > 0 && ((rPressed && !waitingForRelease) || statsOverlayOpen)) {
            // HERE: Reload
            autoReloadBlinkingTimer+=Main.toSlowMotion(1d);
            if(autoReloadBlinkingTimer >= autoReloadBlinkingTime) {
                autoReloadBlinkingTimer = 0;
            }
            if(rPressed || (ammo <= autoReloadMaximumAmmo)) {
                autoReloadTimer+=Main.toSlowMotion(1d);

                if(autoReloadTimer >= autoReloadTime) {
                    if(clip > 0) {
                        clip--;
                        ammo = maxAmmo;
                        autoReloadTimer = 0;
                        autoReloadBlinkingTimer = 0;
                        waitingForRelease = true;
                    }
                }
            }
        } else {
            autoReloadBlinkingTimer = 0;
            autoReloadTimer = 0;
        }

/*
        if(inventoryOpen) {
            timer--;
            if (timer < 0) {
                inventoryOpen = false;
                timer = 0;
            }
        }

        if(i.scroll > 0) {
            selectedItem++;
            if(selectedItem > items.length-1) {
                selectedItem = 0;
            }

            i.scroll = 0;
            inventoryOpen = true;
            timer = time;
        } else if(i.scroll < 0) {
            selectedItem--;
            if(selectedItem < 0) {
                selectedItem = items.length-1;
            }

            i.scroll = 0;
            inventoryOpen = true;
            timer = time;
        }

        if(prevInvOpen && !inventoryOpen) {
            buildingMode = (itemSamples[selectedItem] == null);
        }*/

//        Main.main.getE().getRenderer().setCamX(xx - core.width/2);
//        Main.main.getE().getRenderer().setCamY(yy - core.height/2);
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(p -> p.render(r));
        if(Main.main.showDebugInfo)
            r.fillRectangle(xx, yy, 8, 8, Color.red);
//        int x = Math.floorDiv(Main.main.getE().getInput().getRelativeMouseX(), 16)*16;
//        int y = Math.floorDiv(Main.main.getE().getInput().getRelativeMouseY(), 16)*16;
//        r.fillRectangle(x, y, 16, 16, new Color(40, 100, 70));
    }

    public void renderInventory(Renderer r) {
        int startX = midX - items.length*invSize/2;
        int startY = midY - invSize/2;
        int i = 0;
        for(StructuralBlock block : itemSamples) {
            if(block != null)
                block.render(r, startX + i*invSize+4, startY+4);
            i++;
        }
        r.fillRectangle(startX+selectedItem*invSize, startY, invSize, invSize, new Color(64, 64, 64, 128));
        Font old = r.getG().getFont();
        r.setFont(new Font("Verdana", Font.BOLD, 12));
        r.drawText(itemNames[selectedItem], midX, midY-invSize-4, 12, new Alignment(
                Alignment.HOR_CENTER, Alignment.VER_MIDDLE), new Color(64, 64, 64));
        r.setFont(old);
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

}
