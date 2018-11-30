package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.game.GameObject;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Filter;
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
    private boolean found;
    public boolean buildingMode, inventoryOpen, leftHandShooting, rightHandShooting, clipOverlayOpen;
    public int xx, yy, invSize = 24, midX, midY, selectedItem, time=60, timer, leftHandReload, rightHandReload,
            reloadTime = 50, bulletTimingCap = 5, leftHandBulletTimingCapCounter, leftHandBulletAmountCounter,
            rightHandBulletTimingCapCounter, rightHandBulletAmountCounter, bulletsPerShot = 5, ammo = 34, maxAmmo = 45, clip = 5, maxClip = 10, money;
    public double lastCoinX, lastCoinY, coinOverlayAlpha, coinOverlayX, coinOverlayY, clipOverlayAlpha, clipOverlayRotation, clipOverlayRotationSpeed,
            clipOverlayRotationTarget;
    private int blinkingTime = 30;
    public int[] items;
    private String[] itemNames;
    private StructuralBlock[] itemSamples;
    private static final Color baseColor = new Color(130, 32, 78); // 170, 32, 128
    private float speedX, targetSpeedX, speedY, targetSpeedY, maxSpeed, speedStep, blinkingTimer;

    public Player(int x, int y) {
        super(x, y, 4, 4, 200, 0, 9, false);
        particles = new ArrayList<>();

        leftHandReload = 0;
        rightHandReload = 0;
        leftHandShooting = false;
        rightHandShooting = false;
        leftHandBulletAmountCounter = 0;
        rightHandBulletAmountCounter = 0;
        leftHandBulletTimingCapCounter = 0;
        rightHandBulletTimingCapCounter = 0;

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

        random = new Random();
        core = Main.main.getE();

        this.random = new Random();

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



        if(i.isButtonDown(1) && ammo > 0) {
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

        /*if(!buildingMode) {
            if(i.isButton(1)) {
                int x = Math.floorDiv(i.getRelativeMouseX(), 16)*16;
                int y = Math.floorDiv(i.getRelativeMouseY(), 16)*16;

                GameObject obj = Main.main.find(x+8, y+8);

                boolean pass = obj == null;
                if(!pass) pass = (obj.getClass() != itemSamples[selectedItem].getClass());

                if(pass) {
                    try {
                        if(obj != null) {
                            Main.main.structuralBlocks.remove(obj);
                        }
                        Main.main.structuralBlocks.add(itemSamples[selectedItem].getClass().
                                getDeclaredConstructor(int.class,int.class,int.class).
                                newInstance(x, y, itemSamples[selectedItem].type));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                Main.main.structuralBlocks.add(new StructuralBlock(x, y, StructuralBlock.TYPE_ROCKS));
                Main.main.findOnScreenObjects();
            } else if(i.isButton(3)) {
                int x = Math.floorDiv(i.getRelativeMouseX(), 16)*16;
                int y = Math.floorDiv(i.getRelativeMouseY(), 16)*16;
                Main.main.remove(x+8, y+8);
                Main.main.findOnScreenObjects();
            }
        } else if (i.isButtonDown(3)) {
            // HERE: Shoot
            int dir = (int) AdvancedMath.angle(xx, yy, i.getRelativeMouseX(), i.getRelativeMouseY());
            Arrow arrow = new Arrow(xx, yy, dir + 90);
            Main.main.entities.add(arrow);
            Main.main.findOnScreenObjects();
            Main.main.camera.shake(2f);
            Main.main.camera.bluishEffect = 1f;
        }*/

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

        if(arrowNear || inventoryOpen) {
            Main.slowMotionSpeed -= 0.05f;
            Main.main.camera.bluishEffect += 0.05f;
        } else {
//            System.out.println("SlMoSp: "+Main.slowMotionSpeed);
//            System.out.println("BlFx: "+Main.main.camera.bluishEffect);
            Main.slowMotionSpeed += 0.05f;
            Main.main.camera.bluishEffect -= 0.05f;
        }

        Main.slowMotionSpeed = (float)AdvancedMath.setRange(Main.slowMotionSpeed, 0.25d, 1d);
        Main.main.camera.bluishEffect = (float)AdvancedMath.setRange(Main.main.camera.bluishEffect, 0d, 0.5d);

        coinOverlayAlpha = Math.max(0, coinOverlayAlpha-Main.toSlowMotion(2));
        coinOverlayX += (lastCoinX - coinOverlayX) * 0.1d;
        coinOverlayY += (lastCoinY - coinOverlayY) * 0.1d;

        clipOverlayOpen = i.isKey(KeyEvent.VK_V);

        if(clipOverlayOpen) {
            clipOverlayRotationTarget = 110;
            clipOverlayAlpha += 24;
        } else {
            clipOverlayRotationTarget = 1;
            clipOverlayAlpha -= 24;
        }

        clipOverlayRotation += (clipOverlayRotationTarget - clipOverlayRotation) * 0.1d;
        clipOverlayAlpha = AdvancedMath.setRange(clipOverlayAlpha, 0, 255);

        if(i.isKeyDown(KeyEvent.VK_R)) {
            // HERE: Reload
            clip--;
            ammo = maxAmmo;
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
        int x = Math.floorDiv(Main.main.getE().getInput().getRelativeMouseX(), 16)*16;
        int y = Math.floorDiv(Main.main.getE().getInput().getRelativeMouseY(), 16)*16;
        r.fillRectangle(x, y, 16, 16, new Color(40, 100, 70));
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

    public class PlayerParticle {
        int x, y, dir, alpha, size, alphaSpeed;
        double xD, yD;
        float speed;
        boolean dead;

        Color color, baseColor = new Color(170, 32, 128);

        public PlayerParticle(int x, int y, int size, int dir, float speed, int tone, int alphaSpeed) {
            this.x = x;
            this.y = y;
            this.dir = dir;
            this.speed = speed;
            this.size = size;
            this.dead = false;
            this.alphaSpeed = alphaSpeed;
            xD = x;
            yD = y;
            alpha = 255;
            color = new Color(baseColor.getRed()+tone,
                              baseColor.getGreen()+tone,
                              baseColor.getBlue()+tone);
        }

        public void update() {
            if(dead) return;
//            xD += Math.cos(Math.toRadians(dir))*speed;
//            yD += Math.sin(Math.toRadians(dir))*speed;

//            speed /= 0.9;

            alpha -= alphaSpeed;

            if(alpha < 0) {
                dead = true;
                return;
            }

            this.color = new Color(
                    calcColorParameter(Main.grassColor.getRed(), baseColor.getRed(), alpha/255f),
                    calcColorParameter(Main.grassColor.getGreen(), baseColor.getGreen(), alpha/255f),
                    calcColorParameter(Main.grassColor.getBlue(), baseColor.getBlue(), alpha/255f));
//            System.out.println(color);

            x = (int)xD;
            y = (int)yD;
        }

        public int calcColorParameter(int colorBack, int colorFront, float alpha) {
            return (int)(alpha * colorFront + (1 - alpha) * colorBack);
        }

        public void render(Renderer r) {
            if(dead) return;
            r.fillRectangle(x-size/2, y-size/2, size, size, color);
        }
    }
}
