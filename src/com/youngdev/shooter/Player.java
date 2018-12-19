package com.youngdev.shooter;

import com.engine.Game;
import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.util.*;

public class Player extends Healable {

    public ArrayList<UniParticle> particles;
    private Random random;
    private AABBCollisionManager cm;
    boolean blinkingON;
    private boolean found;
    private boolean waitingForRelease;
    public boolean buildingMode, inventoryOpen, leftHandShooting, rightHandShooting, clipOverlayOpen;
    public int xx, yy, invSize = 24, midX, midY, selectedItem, time=60, timer, leftHandReload, rightHandReload,
            reloadTime = 50, bulletTimingCap = 5, leftHandBulletTimingCapCounter, leftHandBulletAmountCounter,
            rightHandBulletTimingCapCounter, rightHandBulletAmountCounter, bulletsPerShot = 5, ammo = 34, maxAmmo = 45,
            clip = 10, money;
    public double lastCoinX, lastCoinY, coinOverlayAlpha, coinOverlayX, coinOverlayY, clipOverlayAlpha,
            clipOverlayRotation, clipOverlayRotationSpeed, clipOverlayRotationTarget, health, healthMax,
            hunger, hungerMax, statsOverlayAlpha, statsOverlayRotation, statsOverlayRotationSpeed,
            statsOverlayRotationTarget, autoReloadBlinkingTime, autoReloadBlinkingTimer, autoReloadTime,
            autoReloadTimer, autoReloadMaximumAmmo, autoReloadY, autoReloadTargetY, slowMotionSpeedMultiplierTarget,
            raysAccuracy = 1;
    private int blinkingTime = 30;
    public int[] items;
    private String[] itemNames;
    private StructuralBlock[] itemSamples;
    private static Color baseColor = new Color(170, 172, 78); // 170, 32, 128
    private static int statsReloadTargetHeight = -56-12-16;
    private static int reloadTargetHeight = -16;
    private float speedX, targetSpeedX, speedY, targetSpeedY, maxSpeed, speedStep, blinkingTimer;
    private ArrayList<Vector8> rays;
    public ShadowRenderer shadowRenderer;

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

        slowMotionSpeedMultiplierTarget = 1d;

        rays = new ArrayList<>();

        shadowRenderer = new ShadowRenderer();

        mask = new Mask.Rectangle(x-4, y-4, 8, 8);
        cm = new AABBCollisionManager(this, Main.collisionMap);
    }

    @Override
    public void update(Input i) {
        boolean prevInvOpen = inventoryOpen;
        blinkingTimer += 1d/Main.toSlowMotion(1d);

        if(blinkingTimer >= blinkingTime) {
            blinkingON = !blinkingON;
            blinkingTimer = 0;
        }

        // HERE: Use keyboard to move
        int moveX = (i.isKey(KeyEvent.VK_D) ? 1 : 0) - (i.isKey(KeyEvent.VK_A) ? 1 : 0);
        int moveY = (i.isKey(KeyEvent.VK_S) ? 1 : 0) - (i.isKey(KeyEvent.VK_W) ? 1 : 0);

        if((moveX != 0 || moveY != 0)) {
            timer = time;
        }

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
                    clip--;
                    ammo = maxAmmo;
                    autoReloadTimer = 0;
                    autoReloadBlinkingTimer = 0;
                    waitingForRelease = true;
                }
            }
        } else {
            autoReloadBlinkingTimer = 0;
            autoReloadTimer = 0;
        }

        /*
        // ---- Shadow caster V 0.1 ----
        rays.clear();
        long timeStart = System.nanoTime();
        double rayAccuracy = 1d/ raysAccuracy;
        double maxDistance = core.width;
        long averageTime = 0;
        for(double j = 0; j < 360; j+=rayAccuracy) {
            double addX = Math.cos(Math.toRadians(j));
            double addY = Math.sin(Math.toRadians(j));
            double xx = x;
            double yy = y;
            double distance = 0;

            while(distance < maxDistance) {
                distance = Fly.distance(x, y, xx, yy);
                long timeStart2 = System.nanoTime();
                if(Main.collisionMap.collisionAt((int)xx, (int)yy)) {
                    break;
                }
                long timeEnd2 = System.nanoTime();
                averageTime += timeEnd2-timeStart2;
                xx += addX;
                yy += addY;
            }

            rays.add(new Vector4((int)(xx+addX), (int)(yy+addY), (int)(xx+addX*8), (int)(yy+addY*8)));
        }
        long timeEnd = System.nanoTime();
        System.out.println("Checking rays took an average of "+String.valueOf(averageTime/(360d/rayAccuracy))+"ns");
        System.out.println("Checking rays took a total of "+String.valueOf(averageTime)+"ns");
        System.out.println("Calculating rays took a "+String.valueOf(timeEnd-timeStart)+"ns");*/


    }

    public void castRays() {
        // ---- Shadow Caster V 0.2 ----
//        long start = System.nanoTime();
        rays.clear();
        int iterations = 0;
        int raysCast = 0;
        for(GameObject obj : Main.main.visibleChunkObjects) {
            if(obj.mask == null) continue;
            else if(!(obj.mask instanceof Mask.Rectangle)) continue;
            else if(obj instanceof Healable) continue;
            else if(!obj.solid) continue;
            iterations++;

            Mask.Rectangle mask = (Mask.Rectangle) obj.aabbComponent.area;
            double minAngle = Double.MAX_VALUE;
            Point minPoint = null;
            double maxAngle = Double.MIN_VALUE;
            Point maxPoint = null;
            double currentDistance;

            rays.add(new Vector8((int)mask.x, (int)mask.y,
                    (int)(mask.x+mask.w), (int)mask.y,
                    (int)(mask.x+mask.w), (int)(mask.y+mask.h),
                    (int)mask.x, (int)(mask.y+mask.h)));
            for(int j = 0; j < 4; j++) {
//                Point current = new Point(mask.x + (((j+1) % 2 == 0) ? mask.w : 0), mask.y + ((j % 2 == 0) ? mask.h : 0));
//                Point next = new Point(mask.x + (((j+2) % 2 == 0) ? mask.w : 0), mask.y + (((j+1) % 2 == 0) ? mask.h : 0));

                Point current = findCorner(j, mask);
                Point next = findCorner(j+1, mask);

                double disCurrent = Fly.angle(x, y, current.x, current.y);
                double disNext = Fly.angle(x, y, next.x, next.y);

                double size = Fly.distance(0, 0, core.width/2d, core.height/2d);

                rays.add(new Vector8((int)current.x, (int)current.y, (int)(current.x+ Math.cos(Math.toRadians(disCurrent-180))*size),
                                (int)(current.y+ Math.sin(Math.toRadians(disCurrent-180))*size), (int)(next.x+Math.cos(Math.toRadians(disNext-180))*size),
                        (int)(next.y+ Math.sin(Math.toRadians(disNext-180))*size), (int)next.x, (int)next.y));
                raysCast++;
            }

            /*if(minPoint != null && maxPoint != null) {
                rays.add(new Vector8((int) minPoint.x, (int) minPoint.y,
                        (int) (x + Math.cos(Math.toRadians(minAngle-180)) * 320d),
                        (int) (y + Math.sin(Math.toRadians(minAngle-180)) * 320d),
                        (int) (x + Math.cos(Math.toRadians(maxAngle-180)) * 320d),
                        (int) (y + Math.sin(Math.toRadians(maxAngle-180)) * 320d),
                        (int) maxPoint.x, (int) maxPoint.y));
                raysCast++;
            }*/

        }
//        long end = System.nanoTime();
//        System.out.println("Casted "+raysCast+" rays of "+iterations+" objects, took "+(end-start)+" ns");
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

    public void renderRays(Renderer r) {
        r.setColor(new Color(
                Main.grassColor.getRed()-15,
                Main.grassColor.getGreen()-15,
                Main.grassColor.getBlue()-15
        ));
        int addX = (int)-Main.main.camera.cX;
        int addY = (int)-Main.main.camera.cY;
        Graphics2D g2d = (Graphics2D) r.getG();
        g2d.setStroke(new BasicStroke(1));
//        for(Vector4 vec : rays)
//            r.getG().drawLine(vec.x1+addX, vec.y1+addY, vec.x2+addX, vec.y2+addY);
//        g2d.translate((double)addX, (double)addY);
        for(Vector8 vec : rays) {
            if(Main.main.showDebugInfo)
                g2d.drawPolygon(new int[]{vec.x1+addX, vec.x2+addX, vec.x3+addX, vec.x4+addX},
                        new int[]{vec.y1+addY, vec.y2+addY, vec.y3+addY, vec.y4+addY}, 4);
            else {
                g2d.fill(new Polygon(new int[]{vec.x1+addX, vec.x2+addX, vec.x3+addX, vec.x4+addX},
                        new int[]{vec.y1+addY, vec.y2+addY, vec.y3+addY, vec.y4+addY}, 4));
//                g2d.fillPolygon(new int[]{vec.x1, vec.x4, vec.x3},
//                        new int[]{vec.y1, vec.y4, vec.y3}, 3);
//                g2d.fillPolygon(new int[]{vec.x1, vec.x2, vec.x3},
//                        new int[]{vec.y1, vec.y2, vec.y3}, 3);
            }
//            g2d.fillOval(vec.x1-4, vec.y1-4, 8, 8);
//            g2d.fillOval(vec.x2-4, vec.y2-4, 8, 8);
//            g2d.fillOval(vec.x3-4, vec.y3-4, 8, 8);
//            g2d.fillOval(vec.x4-4, vec.y4-4, 8, 8);
        }
//        g2d.translate((double)-addX, (double)-addY);
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    public static class Vector4 {
        public int x1, y1, x2, y2;

        public Vector4(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static class Vector8 {
        public int x1, y1, x2, y2, x3, y3, x4, y4;

        public Vector8(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = x3;
            this.y3 = y3;
            this.x4 = x4;
            this.y4 = y4;
        }
    }

    static Point getIntersectionPoint(Point A, Point B, Point P) {
        Point abDir = B.minus(A);
        Point perpareDir = new Point(-abDir.x, abDir.y);
        Point apDir = P.minus(A);
        double s = (perpareDir.y * apDir.x - perpareDir.x * apDir.y)
                / (abDir.x * perpareDir.y - abDir.y * perpareDir.x);
        return A.plus(abDir.scale(s));
    }

    public static class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Point scale(double k) {
            return new Point(x * k, y * k);
        }

        Point plus(Point add) {
            return new Point(x + add.x, y + add.y);
        }

        Point minus(Point minus) {
            return new Point(x - minus.x, y - minus.y);
        }
    }

    public Point findCorner(int number, Mask.Rectangle mask) {
        switch (number%4) {
            case 0:
                return new Point(mask.x, mask.y);
            case 1:
                return new Point(mask.x+mask.w, mask.y);
            case 2:
                return new Point(mask.x+mask.w, mask.y+mask.h);
            case 3:
                return new Point(mask.x, mask.y+mask.h);
            default:
                return null;
        }
    }

    public class ShadowRenderer extends GameObject {

        public ShadowRenderer() {
            super(14, 0);
        }

        @Override
        public void update(Input input) {

        }

        @Override
        public void render(Renderer r) {
            renderRays(r);
        }

        @Override
        public String shareSend() {
            return null;
        }

        @Override
        public void shareReceive(String s) {

        }
    }

}
